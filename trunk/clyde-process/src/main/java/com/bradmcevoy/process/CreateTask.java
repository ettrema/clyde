
package com.bradmcevoy.process;

import org.jdom.Element;

public class CreateTask implements ActionHandler{

    String nameExpr;
    String template;
    
    public void populateXml(Element el) {
        this.nameExpr = el.getAttributeValue("nameExpression");
        this.template = el.getAttributeValue("template");
    }

    public void process(ProcessContext context) {
        
    }

}
