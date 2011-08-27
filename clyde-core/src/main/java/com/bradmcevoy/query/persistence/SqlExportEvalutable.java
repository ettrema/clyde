package com.bradmcevoy.query.persistence;

import com.bradmcevoy.web.query.Query;
import com.bradmcevoy.web.Folder;
import com.ettrema.db.Table;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.Evaluatable;

import com.bradmcevoy.web.query.Selectable;
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
