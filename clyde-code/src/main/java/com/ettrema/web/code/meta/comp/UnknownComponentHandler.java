package com.ettrema.web.code.meta.comp;

import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author HP
 */
public class UnknownComponentHandler implements ComponentHandler {

	public Class getComponentClass() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getAlias() {
		return "c:unknown";
	}

	public Element toXml(Component c) {
		Element el = new Element("unknown", CodeMeta.NS);
		c.toXml(c.getContainer(), el);
	
		return el;
	}

	public Component fromXml(CommonTemplated res, Element el) {
		Element elComp = el.getChild("component");
		Component c = (Component) XmlUtils2.restoreObject( elComp, res );
		return (Component) c;
	}
	
}
