package com.bradmcevoy.query.persistence;

import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.EvaluatableToXml;
import com.bradmcevoy.web.query.Query;
import com.bradmcevoy.web.query.QueryEvaluatableToXml;
import com.ettrema.db.Table;
import com.ettrema.db.TableXmlHelper;
import org.jdom.Element;
import org.jdom.Namespace;
/**
 * @author brad
 */
public class SqlExportEvaluatableToXml implements EvaluatableToXml<SqlExportEvalutable> {

	private TableXmlHelper tableXmlHelper = new TableXmlHelper(Namespace.getNamespace( "c", "http://clyde.ettrema.com/ns/core" ));
	
	private final QueryEvaluatableToXml queryToXml = new QueryEvaluatableToXml();
	
    @Override
    public String getLocalName() {
        return "sqlexport";
    }

    @Override
    public void populateXml(Element elEval, SqlExportEvalutable target, Namespace ns) {
		populateTable(elEval, target.getTable(), ns);
        populateQuery(elEval, target.getQuery(), ns);

    }

    @Override
    public SqlExportEvalutable fromXml(Element elEval, Namespace ns, Addressable container) {
		System.out.println("fromXml: " + elEval.getName());
        SqlExportEvalutable sqlExport = new SqlExportEvalutable();
        
        updateQuery(sqlExport, elEval, ns, container);
		updateTable(sqlExport, elEval, ns, container);
        return sqlExport;
    }

    @Override
    public Class<SqlExportEvalutable> getEvalClass() {
        return SqlExportEvalutable.class;
    }

    private void populateQuery(Element elEval, Query query, Namespace ns) {
		Element elQuery = new Element("query", ns);
		elEval.addContent(elQuery);
        queryToXml.populateXml(elQuery, query, ns);
    }

    private void updateQuery(SqlExportEvalutable sqlExport, Element elEval, Namespace ns, Addressable container) {
		Element elQuery = elEval.getChild("query", ns);
        Query query = queryToXml.fromXml(elQuery, ns, container);
        sqlExport.setQuery(query);
    }

	private void populateTable(Element elEval, Table table, Namespace ns) {
		Element el = tableXmlHelper.toXml(table);
		elEval.addContent(el);
	}

	private void updateTable(SqlExportEvalutable sqlExport, Element elEval, Namespace ns, Addressable container) {
		System.out.println("updatetable");
		Element elTable = elEval.getChild("table", tableXmlHelper.getNs());
		System.out.println("table el " + elTable.getName());
		Table table = tableXmlHelper.fromXml(elTable);
		sqlExport.setTable(table);
	}

}
