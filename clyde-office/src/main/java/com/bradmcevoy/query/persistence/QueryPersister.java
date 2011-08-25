package com.bradmcevoy.query.persistence;

import java.util.Map;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.query.FieldSource;
import com.bradmcevoy.web.query.FieldSourceMap;
import com.bradmcevoy.web.query.Selectable;
import com.ettrema.db.Table;
import com.ettrema.db.dialects.Dialect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ettrema.context.RequestContext._;

/**
 * Saves the query results to a database table, which is generated on demand
 *
 * @author bradm
 */
public class QueryPersister {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(QueryPersister.class);
	private final Dialect dialect;

	@Autowired
	public QueryPersister( Dialect dialect) {
		this.dialect = dialect;
	}

	public void persist(final Table table, Selectable query, Folder from, String tableName) {
		log.trace("persist");
		Connection con = _(Connection.class);

		if (dialect.tableExists(tableName, con)) {
			table.dropTable(con);
		}
		table.createTable(con, dialect);
		final PreparedStatement stmt = table.prepareInsertStatement(con);
		long count = query.processRows(from, new Selectable.RowProcessor() {

			public void process(FieldSource row) {
				Map<String, Object> values = new FieldSourceMap(row);
				table.insert(stmt, values);
			}
		});
		log.info("inserted rows: " + count + " into: " + table.tableName);
	}
}
