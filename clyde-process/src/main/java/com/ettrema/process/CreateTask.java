
package com.ettrema.process;

import com.bradmcevoy.process.ActionHandler;
import com.bradmcevoy.process.ProcessContext;
import org.jdom.Element;

public class CreateTask implements ActionHandler{

    String nameExpr;
    String template;
    
	@Override
    public void populateXml(Element el) {
        this.nameExpr = el.getAttributeValue("nameExpression");
        this.template = el.getAttributeValue("template");
    }

	@Override
    public void process(ProcessContext context) {
        
    }

}
