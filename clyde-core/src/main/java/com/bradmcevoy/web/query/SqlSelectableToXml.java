package com.bradmcevoy.web.query;

import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.EvaluatableToXml;
import java.util.ArrayList;
import java.util.List;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author bradm
 */
public class SqlSelectableToXml implements EvaluatableToXml<SqlSelectable>{

	@Override
	public String getLocalName() {
		return "sqlquery";
	}

	@Override
	public void populateXml(Element elEval, SqlSelectable target, Namespace ns) {
		Element elSql = new Element("sql", ns);
		elSql.addContent(new CDATA(target.getSql()));
		elEval.addContent(elSql);
		
		Element elFieldNames = new Element("fieldnames", ns);
		elEval.addContent(elFieldNames);
		
		for(String s : target.getFieldNames()) {
			Element elField = new Element("name", ns);
			elField.setText(s);
			elFieldNames.addContent(elField);
		}
	}

	@Override
	public SqlSelectable fromXml(Element elEval, Namespace ns, Addressable container) {
		SqlSelectable selectable = new SqlSelectable();
		Element elSql = elEval.getChild("sql", ns);
		if( elSql != null ) {
			String sql = elSql.getText();
			selectable.setSql(sql);
		}
		Element elFieldNames = elEval.getChild("fieldnames", ns);
		List<String> fieldNames = new ArrayList<String>();
		if(elFieldNames != null ) {
			for(Object oEl : elFieldNames.getChildren("name", ns)) {
				Element elField = (Element) oEl;
				fieldNames.add(elField.getText());
			}
		}
		selectable.setFieldNames(fieldNames);
		return selectable;	
	}

	@Override
	public Class<SqlSelectable> getEvalClass() {
		return SqlSelectable.class;
	}
	
}
