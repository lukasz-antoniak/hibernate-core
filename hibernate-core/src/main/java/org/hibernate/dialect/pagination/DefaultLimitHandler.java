package org.hibernate.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.engine.spi.RowSelection;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class DefaultLimitHandler implements LimitHandler {
	public static final DefaultLimitHandler INSTANCE = new DefaultLimitHandler();

	public boolean supportsLimit() {
		return false;
	}

	public boolean supportsLimitOffset() {
		return supportsLimit();
	}

	public boolean supportsVariableLimit() {
		return supportsLimit();
	}

	public boolean bindLimitParametersInReverseOrder() {
		return false;
	}

	public boolean bindLimitParametersFirst() {
		return false;
	}

	public boolean useMaxForLimit() {
		return false;
	}

	public boolean forceLimitUsage() {
		return false;
	}

	public String getLimitString(String query, int offset, int limit) {
		return getLimitString( query, ( offset > 0 || forceLimitUsage() ) );
	}

	public String getLimitString(String query, boolean hasOffset) {
		throw new UnsupportedOperationException( "Paged queries not supported by " + getClass().getName() );
	}

	public int convertToFirstRowValue(int zeroBasedFirstResult) {
		return zeroBasedFirstResult;
	}

	public String getProcessedSql(String sql, RowSelection selection) {
		boolean hasFirstRow = LimitHelper.getFirstRow( selection ) > 0;
		boolean useLimitOffset = hasFirstRow && useLimit( selection ) && supportsLimitOffset();
		return getLimitString( sql, useLimitOffset ? LimitHelper.getFirstRow( selection ) : 0, getMaxOrLimit( selection ) );
	}

	public int bindLimitParametersAtStartOfQuery(PreparedStatement statement, RowSelection selection, int index)
			throws SQLException {
		return bindLimitParametersFirst() ? bindLimitParameters( statement, selection, index ) : 0;
	}

	public int bindLimitParametersAtEndOfQuery(PreparedStatement statement, RowSelection selection, int index)
			throws SQLException {
		return !bindLimitParametersFirst() ? bindLimitParameters( statement, selection, index ) : 0;
	}

	/**
	 * Default implementation of binding parameter values needed by the dialect-specific LIMIT clause.
	 *
	 * @param statement The statement to which to bind limit parameter values.
	 * @param index The bind position from which to start binding.
	 *
	 * @return The number of parameter values bound.
	 *
	 * @throws java.sql.SQLException Indicates problems binding parameter values.
	 */
	protected int bindLimitParameters(PreparedStatement statement, RowSelection selection, int index)
			throws SQLException {
		if ( !supportsVariableLimit() ) {
			return 0;
		}
		int firstRow = convertToFirstRowValue( LimitHelper.getFirstRow( selection ) );
		int lastRow = getMaxOrLimit( selection );
		boolean hasFirstRow = supportsLimitOffset() && ( firstRow > 0 || forceLimitUsage() );
		boolean reverse = bindLimitParametersInReverseOrder();
		if ( hasFirstRow ) {
			statement.setInt( index + ( reverse ? 1 : 0 ), firstRow );
		}
		statement.setInt( index + ( reverse || !hasFirstRow ? 0 : 1 ), lastRow );
		return hasFirstRow ? 2 : 1;
	}

	public void setMaxRows(PreparedStatement statement, RowSelection selection) throws SQLException {
	}

	/**
	 * Should we pre-process the SQL string, adding a dialect-specific
	 * LIMIT clause.
	 */
	public boolean useLimit(RowSelection selection) {
		return supportsLimit() && LimitHelper.hasMaxRows( selection );
	}

	/**
	 * Some dialect-specific LIMIT clauses require the maximum last row number
	 * (aka, first_row_number + total_row_count), while others require the maximum
	 * returned row count (the total maximum number of rows to return).
	 *
	 * @return The appropriate value to bind into the limit clause.
	 */
	protected int getMaxOrLimit(RowSelection selection) {
		final int firstRow = convertToFirstRowValue( LimitHelper.getFirstRow( selection ) );
		final int lastRow = selection.getMaxRows();
		return useMaxForLimit() ? lastRow + firstRow : lastRow;
	}
}
