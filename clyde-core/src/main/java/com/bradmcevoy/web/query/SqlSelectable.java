package com.bradmcevoy.web.query;

import com.bradmcevoy.utils.LogUtils;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.Evaluatable;
import java.sql.Connection;
import com.bradmcevoy.web.Folder;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.ettrema.context.RequestContext._;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author bradm
 */
public class SqlSelectable implements Selectable, Serializable, Evaluatable {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SqlSelectable.class);
	private static final long serialVersionUID = 1L;
	private Evaluatable sqlEval;
	private List<String> fieldNames;
	private List<Evaluatable> parameters;

	@Override
	public List<String> getFieldNames() {
		return fieldNames;
	}

	@Override
	public List<FieldSource> getRows(Folder from) {
		log.trace("getRows(from)");
		if( sqlEval == null) {
			log.warn("getRows: No SqlEval property - nothing to run");
			return new ArrayList<FieldSource>();
		}
		Object oSql = sqlEval.evaluate(from).toString();
		LogUtils.trace(log, "getRows: sql", oSql);
		String sql = oSql.toString();
		try {
			Connection con = _(Connection.class);			
			PreparedStatement stmt = con.prepareStatement(sql);
			loadParameters(stmt, from);
			ResultSet rs = stmt.executeQuery();
			List<FieldSource> list = new ArrayList<FieldSource>();
			while (rs.next()) {
				FieldSource fs = toFieldSource(rs);
				list.add(fs);
			}
			return list;
		} catch (SQLException ex) {
			throw new RuntimeException(sql, ex);
		}
	}

	@Override
	public long processRows(Folder from, RowProcessor rowProcessor) {
		log.trace("processRows");
		String sql = sqlEval.evaluate(from).toString();
		try {
			Connection con = _(Connection.class);
			PreparedStatement stmt = con.prepareStatement(sql);
			loadParameters(stmt, from);
			ResultSet rs = stmt.executeQuery();
			long count = 0;
			while (rs.next()) {
				FieldSource fs = toFieldSource(rs);
				rowProcessor.process(fs);
				count++;
			}
			return count;
		} catch (SQLException ex) {
			throw new RuntimeException(sql, ex);
		}
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public Evaluatable getSql() {
		return sqlEval;
	}

	public void setSql(Evaluatable sql) {
		this.sqlEval = sql;
	}

	public List<Evaluatable> getParameters() {
		return parameters;
	}

	public void setParameters(List<Evaluatable> parameters) {
		this.parameters = parameters;
	}

	private FieldSource toFieldSource(ResultSet rs) {
		return new SqlFieldSource(rs);
	}

	@Override
	public Object evaluate(RenderContext rc, Addressable from) {
		log.info("evaluate query");
		CommonTemplated relativeTo = (CommonTemplated) from;
		if (!(relativeTo instanceof Folder)) {
			relativeTo = relativeTo.getParentFolder();

		}
		List<FieldSource> list = getRows((Folder) relativeTo);
		log.trace("evaluate returned rows: " + list.size());
		return list;
	}

	@Override
	public Object evaluate(Object from) {
		Folder relativeTo = (Folder) from;
		List<FieldSource> list = getRows(relativeTo);
		log.trace("evaluate returned rows: " + list.size());
		return list;
	}

	@Override
	public void pleaseImplementSerializable() {
	}

	private void loadParameters(PreparedStatement stmt, Folder from) {
		int i = 1;
		for (Evaluatable ev : parameters) {
			Object o = ev.evaluate(from);
			if (o != null) {
				try {
					LogUtils.trace(log, "loadParameters", i, o, o.getClass());
					stmt.setObject(i, o);
					i++;					
				} catch (SQLException ex) {
					throw new RuntimeException("Exception setting parameter: " + i + " to value: " + o + " of type: " + o.getClass(), ex);
				}
			} else{
				log.trace("Got null value for parameter " + i + ". Null values are not supported so skipping it");
			}
		}
	}

	private class SqlFieldSource implements FieldSource {

		private final Map<String, Object> map = new HashMap<String, Object>();

		public SqlFieldSource(ResultSet rs) {
			for (String s : fieldNames) {
				Object o;
				try {
					o = rs.getObject(s);
				} catch (SQLException ex) {
					throw new RuntimeException(s, ex);
				}
				map.put(s, o);
			}
		}

		@Override
		public Object get(String name) {
			return map.get(name);
		}

		@Override
		public Object getData() {
			return map;
		}

		@Override
		public Set<String> getKeys() {
			return new HashSet<String>(fieldNames);
		}
	}
}
