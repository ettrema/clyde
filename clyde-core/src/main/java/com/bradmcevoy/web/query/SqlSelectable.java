package com.bradmcevoy.web.query;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.Evaluatable;
import java.sql.CallableStatement;
import java.sql.Connection;
import com.bradmcevoy.web.Folder;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.ettrema.context.RequestContext._;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bradm
 */
public class SqlSelectable implements Selectable, Serializable, Evaluatable {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SqlSelectable.class);
	private static final long serialVersionUID = 1L;
	
	private String sql;
	
	private List<String> fieldNames;
	
	@Override
	public List<String> getFieldNames() {
		return fieldNames;
	}

	@Override
	public List<FieldSource> getRows(Folder from) {
		try {
			Connection con = _(Connection.class);
			CallableStatement stmt = con.prepareCall(sql);
			ResultSet rs = stmt.executeQuery();
			List<FieldSource> list = new ArrayList<FieldSource>();
			while(rs.next()) {
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
		try {
			Connection con = _(Connection.class);
			CallableStatement stmt = con.prepareCall(sql);
			ResultSet rs = stmt.executeQuery();
			long count = 0;
			while(rs.next()) {
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

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
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
        List<FieldSource> list =  getRows(relativeTo);
		log.trace("evaluate returned rows: " + list.size());
		return list;
    }

	@Override
	public void pleaseImplementSerializable() {

	}
	
	private class SqlFieldSource implements FieldSource {

		private final Map<String,Object> map = new HashMap<String, Object>();
		
		public SqlFieldSource(ResultSet rs) {
			for(String s : fieldNames) {
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
