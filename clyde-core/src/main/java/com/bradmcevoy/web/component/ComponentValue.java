package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.*;
import com.bradmcevoy.xml.XmlHelper;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

public class ComponentValue implements Component, Serializable, ValueHolder {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ComponentValue.class );
    private static final long serialVersionUID = 1L;
    public String name;
    public Object value;
    private Addressable parent;
    private transient String validationMessage;

    public ComponentValue( String name, Object value ) {
        this.name = name;
        this.value = value;
    }

    public ComponentValue( Element el, CommonTemplated container ) {
        this.name = el.getAttributeValue( "name" );
        log.debug( "created: " + this.name);
        String sVal = InitUtils.getValue( el );
        ComponentDef def = getDef( container );
        if( def == null ) {
            this.value = sVal;
        } else {
            log.debug( "parse val");
            this.value = def.parseValue( this, container, sVal );
        }
    }

    public void setValidationMessage( String validationMessage ) {
        this.validationMessage = validationMessage;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    /**
     * placeholder, called after parent resource is saved
     * 
     * returns true if a change occured which must be saved
     */
    public boolean afterSave() {
        Object val = getValue();
        if( val == null ) return false;
        if( val instanceof AfterSavable ) {
            return ( (AfterSavable) val ).afterSave();
        } else {
            return false;
        }
    }

    @Override
    public void init( Addressable parent ) {
        this.parent = parent;
    }

    @Override
    public Addressable getContainer() {
        return parent;
    }

    @Override
    public boolean validate( RenderContext rc ) {
        return getDef( rc ).validate( this, rc );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "componentValue" );
        el.addContent( e2 );
        String clazzName = this.getClass().getName();
        log.debug( "toXml: " + name);
        if( !clazzName.equals( ComponentValue.class.getName() ) ) { // for brevity, only add class where not default
            e2.setAttribute( "class", clazzName );
        }
        e2.setAttribute( "name", name );
        String v = getFormattedValue( (CommonTemplated) container );
        XmlHelper helper = new XmlHelper();
        List content = helper.getContent( v );
        e2.setContent( content );
        return e2;
    }

    @Override
    public String toString() {
        try {
            CommonTemplated ct = (CommonTemplated) this.getContainer();
            if( ct != null ) {
                RenderContext rc = new RenderContext( ct.getTemplate(), ct, null, true );
                String s = this.render( rc );
                return s;
            } else {
                Object o = this.getValue();
                return o == null ? "" : o.toString();
            }
        } catch( Exception e ) {
            log.error( "exception rendering componentvalue: " + this.getName(), e );
            return "ERR: " + this.getName() + ": " + e.getMessage();
        }

    }

    @Override
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public Object typedValue( Page page ) {
        Object val = getValue();
        if( val == null ) return null;
        if( val instanceof String ) {
            return getDef( page ).parseValue( this, page, (String) val );
        } else {
            return val;
        }
    }

    public void setValue( Object value ) {
        this.value = value;
    }

    public ComponentDef getDef( RenderContext rc ) {
        if( rc == null ) return null;
        return getDef( rc.page );
//        Template templatePage = rc.template;
//        if( templatePage == null ) {
//            templatePage = (Template) rc.page; // if the page doesnt have a template, use itself as template
//        }
//        ComponentDef def = templatePage.getComponentDef(name);
//        if( def == null ) {
//            log.warn("did not find componentdef for: " + name);
//        }
//        return def;
    }

    /**
     * Locates this values definition from the given pages template
     * 
     * @param page
     * @return
     */
    public ComponentDef getDef( Templatable page ) {
        ITemplate templatePage = page.getTemplate();
        if( templatePage == null ) {
            return null;
        }
        ComponentDef def = templatePage.getComponentDef( name );
        if( def == null ) {
            log.warn( "did not find componentdef for: " + name + " in template: " + templatePage.getName() );
        }
        return def;
    }

    @Override
    public String render( RenderContext rc ) {
        ComponentDef def = getDef( rc );
        if( def == null ) return "";
        return def.render( this, rc );
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        ComponentDef def = getDef( rc );
        if( def == null ) {
            return "";
        }
        return def.renderEdit( this, rc );
    }

    @Override
    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        init( rc.page );
        ComponentDef def = getDef( rc );
        if( def == null ) {
            log.warn( "Could not find definition for : " + this.name );  // this can happen when changing templates
        } else {
            def.onPreProcess( this, rc, parameters, files );
        }
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;
    }

    private String getFormattedValue( CommonTemplated container ) {
        ComponentDef def = getDef( container );
        if( def == null ) {
            if( value == null ) return "";
            return value.toString();
        }
        return def.formatValue( value );
    }

    public int getYear() {
        return Formatter.getInstance().getYear( getValue() );
    }

    public int getMonth() {
        return Formatter.getInstance().getMonth( getValue() );
    }
}
