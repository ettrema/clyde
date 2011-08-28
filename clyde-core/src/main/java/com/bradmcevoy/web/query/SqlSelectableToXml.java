package com.bradmcevoy.web.query;

import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import com.bradmcevoy.web.eval.EvaluatableToXml;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author bradm
 */
public class SqlSelectableToXml implements EvaluatableToXml<SqlSelectable> {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SqlSelectableToXml.class);
	
	@Override
	public String getLocalName() {
		return "sqlquery";
	}

	@Override
	public void populateXml(Element elEval, SqlSelectable target, Namespace ns) {
		Element elSql = new Element("sql", ns);
		EvalUtils.setEvalDirect(elSql, target.getSql(), ns);
		elEval.addContent(elSql);

		Element elFieldNames = new Element("fieldnames", ns);
		elEval.addContent(elFieldNames);

		if (target.getFieldNames() != null) {
			for (String s : target.getFieldNames()) {
				Element elField = new Element("name", ns);
				elField.setText(s);
				elFieldNames.addContent(elField);
			}
		}

		Element elParameters = new Element("parameters", ns);
		elEval.addContent(elParameters);
		if (target.getParameters() != null) {
			for (Evaluatable ev : target.getParameters()) {
				Element elParam = new Element("parameter", ns);
				elParameters.addContent(elParam);
				EvalUtils.setEvalDirect(elParam, ev, ns);
			}
		}
	}

	@Override
	public SqlSelectable fromXml(Element elEval, Namespace ns, Addressable container) {
		SqlSelectable selectable = new SqlSelectable();
		Element elSql = elEval.getChild("sql", ns);
		if (elSql != null) {
			Evaluatable sqlEval = EvalUtils.getEvalDirect(elSql, ns, container);
			if( sqlEval == null ) {
				log.warn("Found a sql element, but did not parse to an Evaluatable element");
			}
			selectable.setSql(sqlEval);
			System.out.println("set sql eval: " + sqlEval);
		} else {
			log.warn("No sql element!!");
		}
		Element elFieldNames = elEval.getChild("fieldnames", ns);
		List<String> fieldNames = new ArrayList<String>();
		if (elFieldNames != null) {
			for (Object oEl : elFieldNames.getChildren("name", ns)) {
				Element elField = (Element) oEl;
				fieldNames.add(elField.getText());
			}
		}
		selectable.setFieldNames(fieldNames);

		Element elParameters = elEval.getChild("parameters", ns);
		List<Evaluatable> parameters = new ArrayList<Evaluatable>();
		if (elParameters != null) {
			System.out.println("have params");
			for (Object oEl : elParameters.getChildren("parameter", ns)) {
				System.out.println("have param");
				Element elParam = (Element) oEl;
				Evaluatable ev = EvalUtils.getEvalDirect(elParam, ns, container);
				parameters.add(ev);
			}
		}
		selectable.setParameters(parameters);

		return selectable;
	}

	@Override
	public Class<SqlSelectable> getEvalClass() {
		return SqlSelectable.class;
	}
}
