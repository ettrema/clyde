
package com.bradmcevoy.web;

import com.bradmcevoy.web.component.InitUtils;
import java.io.Serializable;
import org.jdom.Element;

/**
 * Specifies rules for aggregating files into VirtualFolders
 * 
 * @author brad
 */
public class AggregationSpec implements Serializable{
    
    private static final long serialVersionUID = 1L;    
    
    /**
     * Find a suitable child element of the given element and create
     * an AggregationSpec from it. Recursively loads child elements
     * 
     * @param el
     * @return
     */
    public static AggregationSpec loadFromXml(Element el) {
        Element elThis = el.getChild("aggregationSpec");
        if( elThis == null ) return null;
        AggregationSpec spec = new AggregationSpec();
        spec.expr = InitUtils.getValue(elThis, "expression");
        spec.template = InitUtils.getValue(elThis, "template");
        spec.child = loadFromXml(elThis);
        return spec;
    }    
    
    // The name of the template to assign to the virtual folder
    String template;
    
    String expr;    
    
    
    /**
     * if null, children are just added to a virtual folder
     */
    AggregationSpec child;
    

    /**
     * Format this object into xml and append it the given element
     * 
     * @param el
     * @return
     */
    public Element toXml(Element el) {        
        Element elThis = new Element("aggregationSpec");
        el.addContent(elThis);
        InitUtils.setString(elThis, "expression", expr);
        InitUtils.setString(elThis, "template", template);
        if( child != null ) {
            child.toXml(elThis);
        }
        return elThis;
    }
    
    
}
