/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.JDBCException;
import org.hibernate.LockMode;
import org.hibernate.QueryTimeoutException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.StandardBasicTypes;

/**
 * A dialect for Microsoft SQL 2005. (HHH-3936 fix)
 *
 * @author Yoryos Valotasios
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class SQLServer2005Dialect extends SQLServerDialect {
	private static final String SELECT = "select";
	private static final String FROM = "from";
	private static final String DISTINCT = "distinct";
	private static final String ORDER_BY = "order by";
	private static final int MAX_LENGTH = 8000;

	private static final Pattern ALIAS_PATTERN = Pattern.compile( "(?i)\\sas\\s(.)+$" );

	public SQLServer2005Dialect() {
		// HHH-3965 fix
		// As per http://www.sql-server-helper.com/faq/sql-server-2005-varchar-max-p01.aspx
		// use varchar(max) and varbinary(max) instead of TEXT and IMAGE types
		registerColumnType( Types.BLOB, "varbinary(MAX)" );
		registerColumnType( Types.VARBINARY, "varbinary(MAX)" );
		registerColumnType( Types.VARBINARY, MAX_LENGTH, "varbinary($l)" );
		registerColumnType( Types.LONGVARBINARY, "varbinary(MAX)" );

		registerColumnType( Types.CLOB, "varchar(MAX)" );
		registerColumnType( Types.LONGVARCHAR, "varchar(MAX)" );
		registerColumnType( Types.VARCHAR, "varchar(MAX)" );
		registerColumnType( Types.VARCHAR, MAX_LENGTH, "varchar($l)" );

		registerColumnType( Types.BIGINT, "bigint" );
		registerColumnType( Types.BIT, "bit" );
		registerColumnType( Types.BOOLEAN, "bit" );


		registerFunction( "row_number", new NoArgSQLFunction( "row_number", StandardBasicTypes.INTEGER, true ) );
	}

	@Override
	public boolean supportsLimitOffset() {
		return true;
	}

	@Override
	public boolean bindLimitParametersFirst() {
		return false;
	}

	@Override
	public boolean supportsVariableLimit() {
		return true;
	}

	@Override
	public int convertToFirstRowValue(int zeroBasedFirstResult) {
		// Our dialect paginated results aren't zero based. The first row should get the number 1 and so on
		return zeroBasedFirstResult + 1;
	}

	@Override
	public String getLimitString(String query, int offset, int limit) {
		// We transform the query to one with an offset and limit if we have an offset and limit to bind
		if ( offset > 1 || limit > 1 ) {
			return getLimitString( query, true );
		}
		return query;
	}

	/**
	 * Add a LIMIT clause to the given SQL SELECT (HHH-2655: ROW_NUMBER for Paging)
	 *
	 * The LIMIT SQL will look like:
	 *
	 * <pre>
	 * WITH query AS (
	 *   SELECT inner_query.*
	 *        , ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__
	 *     FROM ( original_query_with_top_if_order_by_present_and_all_aliased_columns ) inner_query
	 * )
	 * SELECT alias_list FROM query WHERE __hibernate_row_nr__ >= offset AND __hibernate_row_nr__ < offset + last
	 * </pre>
	 *
	 * @param querySqlString The SQL statement to base the limit query off of.
	 * @param hasOffset Is the query requesting an offset?
	 *
	 * @return A new SQL statement with the LIMIT clause applied.
	 */
	@Override
	public String getLimitString(String querySqlString, boolean hasOffset) {
		StringBuilder sb = new StringBuilder( querySqlString.trim() );
		if (sb.charAt(sb.length() - 1) == ';') {
			sb.setLength(sb.length() - 1);
		}

		final String selectClause = fillAliasInSelectClause( sb );

		int orderByIndex = shallowIndexOfWord( sb, ORDER_BY, 0 );
		if ( orderByIndex > 0 ) {
			// ORDER BY requires using TOP.
			addTopExpression( sb );
		}

		encloseWithOuterQuery( sb );

		// Wrap the query within a with statement:
		sb.insert( 0, "WITH query AS (" ).append( ") SELECT ").append( selectClause ).append( " FROM query " );
		sb.append( "WHERE __hibernate_row_nr__ >= ? AND __hibernate_row_nr__ < ?" );

		return sb.toString();
	}

	@Override
	public int bindLimitParametersPreQuery(String sql, PreparedStatement statement, int index, int firstRow, int lastRow)
			throws SQLException {
		if ( sql.contains( " " + ORDER_BY + " " ) ) {
			statement.setInt( index, lastRow ); // Binding TOP(?).
			return 1;
		}
		return 0;
	}

	/**
	 * Adds missing aliases in provided SELECT clause and returns coma-separated list of them.
	 * 
	 * @param sb SQL query.
	 * @return List of aliases separated with comas.
	 */
	protected String fillAliasInSelectClause(StringBuilder sb) {
		final List<String> aliases = new LinkedList<String>();
		final int startPos = shallowIndexOf( sb, SELECT_WITH_SPACE, 0 );
		int endPos = shallowIndexOfWord( sb, FROM, startPos );
		int nextComa = startPos;
		int prevComa = startPos;
		int unique = 0;
		
		while ( nextComa != -1 ) {
			prevComa = nextComa;
			nextComa = shallowIndexOf( sb, ",", nextComa );
			if ( nextComa > endPos ) break;
			if ( nextComa != -1 ) {
				String expression = sb.substring( prevComa, nextComa );
				String alias = getAlias( expression );
				if ( alias == null ) {
					// Inserting alias. It is unlikely that we would have to add alias, but just in case.
					alias = StringHelper.generateAlias( "page", unique );
					sb.insert( nextComa, " as " + alias );
					++unique;
					nextComa += ( " as " + alias ).length();
				}
				aliases.add( alias );
				++nextComa;
			}
		}
		// Processing last column.
		endPos = shallowIndexOfWord( sb, FROM, startPos ); // Refreshing end position, because we might have inserted new alias.
		String expression = sb.substring( prevComa, endPos );
		String alias = getAlias( expression );
		if ( alias == null ) {
			// Inserting alias. It is unlikely that we would have to add alias, but just in case.
			alias = StringHelper.generateAlias( "page", unique );
			sb.insert( endPos - 1, " as " + alias );
		}
		aliases.add( alias );

		return StringHelper.join( ", ", aliases.iterator() );
	}

	/**
	 * Returns alias of provided single column selection or {@code null} if not found.
	 * Alias should be preceded with {@code AS} keyword.
	 *
	 * @param expression Single column select expression.
	 * @return Column alias.
	 */
	private String getAlias(String expression) {
		Matcher matcher = ALIAS_PATTERN.matcher( expression );
		if ( matcher.find() ) {
			return matcher.group( 0 ).replaceFirst( "(?i)\\sas\\s", "" ).trim();
		}
		return null;
	}

	/**
	 * Encloses original SQL statement with outer query that provides {@literal __hibernate_row_nr__} column.
	 * 
	 * @param sql SQL query.
	 */
	protected void encloseWithOuterQuery(StringBuilder sql) {
		sql.insert( 0, "SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( " );
		sql.append( " ) inner_query " );
	}

	/**
	 * Adds {@code TOP} expression. Parameter value is bind in
	 * {@link #bindLimitParametersPreQuery(String, PreparedStatement, int, int, int)} method. 
	 *
	 * @param sql SQL query.
	 */
	protected void addTopExpression(StringBuilder sql) {
		final int distinctStartPos = shallowIndexOfWord( sql, DISTINCT, 0 );
		if (distinctStartPos > 0) {
			// Place TOP after DISTINCT.
			sql.insert( distinctStartPos + DISTINCT.length(), " TOP(?)" );
		} else {
			final int selectStartPos = shallowIndexOf( sql, SELECT_WITH_SPACE, 0 );
			// Place TOP after SELECT.
			sql.insert( selectStartPos + SELECT.length(), " TOP(?)" );
		}
	}

	public static final String SELECT_WITH_SPACE = SELECT + ' ';

	@Override // since SQLServer2005 the nowait hint is supported
	public String appendLockHint(LockMode mode, String tableName) {
		if ( mode == LockMode.UPGRADE_NOWAIT ) {
			return tableName + " with (updlock, rowlock, nowait)";
		}
		return super.appendLockHint( mode, tableName );
	}

	/**
	 * Returns index of the first case-insensitive match of search term surrounded by spaces
	 * that is not enclosed in parentheses.
	 *
	 * @param sb String to search.
	 * @param search Search term.
	 * @param fromIndex The index from which to start the search.
	 * @return Position of the first match, or {@literal -1} if not found.
	 */
	private static int shallowIndexOfWord(final StringBuilder sb, final String search, int fromIndex) {
		final int index = shallowIndexOf( sb, ' ' + search + ' ', fromIndex );
		return index != -1 ? ( index + 1 ) : -1; // In case of match adding one because of space placed in front of search term.
	}

	/**
	 * Returns index of the first case-insensitive match of search term that is not enclosed in parentheses.
	 *
	 * @param sb String to search.
	 * @param search Search term.
	 * @param fromIndex The index from which to start the search.
	 * @return Position of the first match, or {@literal -1} if not found.
	 */
	private static int shallowIndexOf(StringBuilder sb, String search, int fromIndex) {
		final String lowercase = sb.toString().toLowerCase(); // case-insensitive match
		final int len = lowercase.length();
		final int searchlen = search.length();
		int pos = -1, depth = 0, cur = fromIndex;
		do {
			pos = lowercase.indexOf( search, cur );
			if ( pos != -1 ) {
				for ( int iter = cur; iter < pos; iter++ ) {
					char c = sb.charAt( iter );
					if ( c == '(' ) {
						depth = depth + 1;
					}
					else if ( c == ')' ) {
						depth = depth - 1;
					}
				}
				cur = pos + searchlen;
			}
		} while ( cur < len && depth != 0 && pos != -1 );
		return depth == 0 ? pos : -1;
	}

	@Override
	public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
		return new SQLExceptionConversionDelegate() {
			@Override
			public JDBCException convert(SQLException sqlException, String message, String sql) {
				final String sqlState = JdbcExceptionHelper.extractSqlState( sqlException );

				if(  "HY008".equals( sqlState )){
					throw new QueryTimeoutException( message, sqlException, sql );
				}
				return null;
			}
		};
	}
}
