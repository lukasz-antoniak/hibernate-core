package org.hibernate.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.engine.spi.RowSelection;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class NoopLimitHandler extends DefaultLimitHandler {
	public static final NoopLimitHandler INSTANCE = new NoopLimitHandler();

	public String getProcessedSql(String sql, RowSelection selection) {
		return sql;
	}

	public int bindLimitParametersAtStartOfQuery(PreparedStatement statement, RowSelection selection, int index) {
		return 0;
	}

	public int bindLimitParametersAtEndOfQuery(PreparedStatement statement, RowSelection selection, int index) {
		return 0;
	}

	public void setMaxRows(PreparedStatement statement, RowSelection selection) throws SQLException {
		if ( LimitHelper.hasMaxRows( selection ) ) {
			statement.setMaxRows( selection.getMaxRows() + convertToFirstRowValue( LimitHelper.getFirstRow( selection ) ) );
		}
	}
}
