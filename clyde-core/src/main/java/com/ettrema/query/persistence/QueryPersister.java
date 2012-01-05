package com.ettrema.query.persistence;

import java.sql.SQLException;
import java.util.Map;
import com.ettrema.web.Folder;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.query.FieldSource;
import com.ettrema.web.query.Selectable;
import com.ettrema.db.Table;
import com.ettrema.db.dialects.Dialect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
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
	public QueryPersister(Dialect dialect) {
		this.dialect = dialect;
	}

	public void persist(final Table table, Selectable query, Folder from) {
		log.trace("persist");
		String tableName = table.tableName;
		Connection con = _(Connection.class);

		if (dialect.tableExists(tableName, con)) {
			table.dropTable(con);
		}
		table.createTable(con, dialect);
		final PreparedStatement stmt = table.prepareInsertStatement(con);
		long count = query.processRows(from, new Selectable.RowProcessor() {

			@Override
			public void process(FieldSource row) {
				Map<String, Object> values = toSimpleValues(row);
				table.insert(stmt, values);
			}
		});
		try {
			con.commit();
		} catch (SQLException ex) {
			log.error("Exception committing",ex);
		}
		log.info("inserted rows: " + count + " into: " + table.tableName);
	}

	private Map<String, Object> toSimpleValues(FieldSource row) {
		Map<String, Object> map = new HashMap<String, Object>();
		for( String key : row.getKeys()) {
			Object value = row.get(key);
			if( value instanceof ComponentValue) {
				ComponentValue cv = (ComponentValue) value;
				value = cv.getValue();
			}
			map.put(key, value);
		}
		return map;
	}
}
