package com.ettrema.query.persistence;

import com.ettrema.web.query.Query;
import com.ettrema.web.Folder;
import com.ettrema.db.Table;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.eval.Evaluatable;

import com.ettrema.web.query.Selectable;
import java.io.Serializable;
import static com.ettrema.context.RequestContext._;

/**
 * When evaluated, will execute the query and persist it to the given
 * table definition
 *
 * @author bradm
 */
public class SqlExportEvalutable implements Evaluatable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Query query;
	
	private Table table;
	
	@Override
	public Object evaluate(RenderContext rc, Addressable from) {
		Templatable t = (Templatable) from;
		Folder folder;
		if( t instanceof Folder ) {
			folder = (Folder) t;
		} else {
			folder = t.getParentFolder();
		}
		_(QueryPersister.class).persist(table, query, folder);
		return null;
	}

	@Override
	public Object evaluate(Object from) {
		_(QueryPersister.class).persist(table, query, (Folder)from);
		return null;
	}

	@Override
	public void pleaseImplementSerializable() {
	}

	Query getQuery() {
		return query;
	}

	void setQuery(Query query) {
		this.query = query;
	}

	Table getTable() {
		return table;
	}

	void setTable(Table table) {
		this.table = table;
	}
	
	
	
}
