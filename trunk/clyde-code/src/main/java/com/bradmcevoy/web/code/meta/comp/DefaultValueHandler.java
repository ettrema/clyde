package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.xml.XmlHelper;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class DefaultValueHandler implements ValueHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultValueHandler.class);

    public static Namespace NS_HTML_DEFAULT = Namespace.getNamespace( "http://www.w3.org/1999/xhtml" );

    public Class getComponentValueClass() {
        return ComponentValue.class;
    }

    public Element toXml( ComponentValue cv, CommonTemplated container ) {
        Element el = new Element( getAlias(), CodeMeta.NS );
        populateXml( el, cv, container );
        return el;
    }

    public void populateXml( Element e2, ComponentValue cv, CommonTemplated container ) {
        e2.setAttribute( "name", cv.getName() );
        String v = cv.getFormattedValue( container );
        List content = XmlHelper.getContent( v );
        e2.setContent( content );
    }

    public String getAlias() {
        return "value";
    }

    public ComponentValue fromXml( CommonTemplated res, Element eAtt ) {
        String name = eAtt.getAttributeValue( "name" );
        ComponentValue cv = new ComponentValue( name, res );
        fromXml( eAtt, res, cv );
        return cv;
    }

    public void fromXml( Element eAtt, CommonTemplated res,  ComponentValue cv ) {
        //String sVal = InitUtils.getValue( eAtt );
        ComponentDef def = getDef( res, cv.getName() );
        if( def == null ) {
            throw new RuntimeException( "No definition for : " + cv.getName() + " in template: " + res.getTemplateName());
//            String sVal = InitUtils.getValue( eAtt );
//            cv.setValue( sVal );
        } else {
            cv.setValue( def.parseValue( cv, res, eAtt ) );
        }
    }

    public ComponentDef getDef( Templatable page, String name ) {
        ITemplate templatePage = page.getTemplate();
        if( templatePage == null ) {
            log.warn("No template for: " + page.getName() + " template name: " + page.getTemplateName());
            return null;
        }
        ComponentDef def = templatePage.getComponentDef( name );
        return def;
    }
}
