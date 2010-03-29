package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.*;
import com.bradmcevoy.xml.XmlHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.XMLReaderFactory;

public class ComponentValue implements Component, Serializable, ValueHolder {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ComponentValue.class );
    private static final long serialVersionUID = 1L;
    public String name;
    public Object value;

    private List<OldValue> oldValues;
    private Addressable parent;
    private transient ThreadLocal<String> thValidationMessage = new ThreadLocal<String>();

    public ComponentValue(String name, Addressable container ) {
        this.name = name;
        this.parent = container;
        this.oldValues = new ArrayList<OldValue>();
    }

    public ComponentValue( Element el, Templatable container ) {
        this.name = el.getAttributeValue( "name" );
        this.oldValues = new ArrayList<OldValue>();
        this.parent = container;
        log.debug( "created: " + this.name );
        String sVal = InitUtils.getValue( el );
        ComponentDef def = getDef( container );
        if( def == null ) {
            log.warn( "no container for CV " + name + ", cant' parse value so it will be a String!!!" );
            this.value = sVal;
        } else {
            log.debug( "parse value of CV" );
            this.value = def.parseValue( this, container, sVal );
        }
    }

    public List<OldValue> getOldValues() {
        if( oldValues == null ) {
            return Collections.EMPTY_LIST;
        } else {
            return Collections.unmodifiableList( oldValues );
        }
    }



    public void setValidationMessage( String validationMessage ) {
        log.debug( "setValidationMessage: " + validationMessage);
        if( thValidationMessage == null ) {
            thValidationMessage = new ThreadLocal<String>();
        }
        thValidationMessage.set( validationMessage);
    }

    public String getValidationMessage() {
        if( thValidationMessage == null ) {
            return null;
        }
        return thValidationMessage.get();
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
        log.debug( "toXml: " + name );
        if( !clazzName.equals( ComponentValue.class.getName() ) ) { // for brevity, only add class where not default
            e2.setAttribute( "class", clazzName );
        }
        e2.setAttribute( "name", name );
        String v = getFormattedValue( (CommonTemplated) container );
//        List l = formatContentToXmlList( v );
//        e2.setContent( l );

        XmlHelper helper = new XmlHelper();
        List content = helper.getContent( v );
        e2.setContent( content );
        return e2;
    }

    List formatContentToXmlList( String content ) {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            try {
                reader.setFeature( "http://xml.org/sax/features/dom/create-entity-ref-nodes", true);
            } catch(Exception e) {
                e.printStackTrace();
            }
            try{
                reader.setFeature( "http://apache.org/xml/features/dom/create-entity-ref-nodes",true);
            } catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println( "using reader: " + reader.getClass());
//            reader.setEntityResolver( new MyEntityResolver());
            ContentParsingSaxHandler hnd = new ContentParsingSaxHandler();
            reader.setContentHandler( hnd );
            String xml = "<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE root PUBLIC '-//MyDT//DTD MYDTD-XML//MYDTD' 'xhtml-lat1.ent'><root>" + content + "</root>";
            reader.parse( new InputSource( new ByteArrayInputStream( xml.getBytes() ) ) );
            return hnd.getContent();
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } catch( SAXException ex ) {
            throw new RuntimeException( ex );
        }

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
//        if( parent == null ) {
//            log.warn( "no parent set. Value might not be typed correctly");
//        }
//        // In some rare (and inexplicable) cases the value is not typed correctly
//        if( parent != null && parent instanceof Page ) {
//            return typedValue( (Page) parent);
//        } else {
//            return value;
//        }
    }

    public Object typedValue( Page page ) {
        Object val = this.value;
        if( val == null ) return null;
        if( val instanceof String ) {
            return getDef( page ).parseValue( this, page, (String) val );
        } else {
            return val;
        }
    }

    public void setValue( Object value ) {
        if( this.value != null && !this.value.equals( value )) {
            RequestParams cur = RequestParams.current();
            String userName = null;
            if( cur != null && cur.getAuth() != null ) {
                User user = (User) cur.getAuth().getTag();
                if( user != null ) {
                    userName = user.getEmailAddress().toString();
                }
            }
            OldValue old = new OldValue( value, new Date(), userName);
            if( oldValues == null ) {
                oldValues = new ArrayList<OldValue>();
            }
            oldValues.add( old );
        }
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
        if( this.parent == null ) {
            this.parent = rc.page;
        }
        return def.render( this, rc );
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        ComponentDef def = getDef( rc );
        if( def == null ) {
            return "";
        }
        if( this.parent == null ) {
            this.parent = rc.page;
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

    public class MyEntityResolver implements EntityResolver2 {

        public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException {
            System.out.println( "resolveEntity: " + publicId + " - " + systemId );
//            return new InputSource( new ByteArrayInputStream( "".getBytes()));
            return null;
        }

        public InputSource getExternalSubset( String name, String baseURI ) throws SAXException, IOException {
            System.out.println( "getExternalSubset: " + name);
            return null;
        }

        public InputSource resolveEntity( String name, String publicId, String baseURI, String systemId ) throws SAXException, IOException {
            System.out.println( "resolveEntity: name: " + name + " publicid: " + publicId + " - " + systemId );
            //return new InputSource( new ByteArrayInputStream( "".getBytes()));
            return null;
        }
    }

    public class ContentParsingSaxHandler extends DefaultHandler {

        private Document doc;
        private StringBuilder sb = new StringBuilder();
        private Stack<Element> elementPath = new Stack<Element>();

        public ContentParsingSaxHandler() {
            DocType docType = new DocType( "root", "-//MyDT//DTD MYDTD-XML//MYDTD", "xhtml-lat1.ent");
            doc = new Document(new Element( "root" ), docType);
        }

        @Override
        public void startElement( String uri, String localName, String name, Attributes attributes ) throws SAXException {
            Element el = new Element( name );
            for( int i = 0; i < attributes.getLength(); i++ ) {
                String attName = attributes.getQName( i );
                String val = attributes.getValue( i );
                el.setAttribute( attName, val );
            }
            Element parent = null;
            if( elementPath.size() > 0 ) {
                parent = elementPath.peek();
                if( sb.length() > 0 ) {
                    String content = sb.toString();
                    parent.addContent( content );
                }
                parent.addContent( el );
            } else {
                doc.setRootElement( el );
            }
            elementPath.push( el );
            super.startElement( uri, localName, name, attributes );
        }

        @Override
        public void characters( char[] ch, int start, int length ) throws SAXException {
            sb.append( ch, start, length );
        }

        @Override
        public void endElement( String uri, String localName, String name ) throws SAXException {
            String content = sb.toString();
            Element el = elementPath.pop();
            el.addContent( content );
            sb.delete( 0, sb.length() );

            super.endElement( uri, localName, name );
        }

        List getContent() {
            List contents = new ArrayList();
            List rootContents = doc.getRootElement().getContent();
            rootContents = new ArrayList( rootContents );
            for( Object o : rootContents ) {
                if( o instanceof Content ) {
                    Content c = (Content) o;
                    c.detach();
                }
                contents.add( o );
            }
            return contents;
        }
    }

    public static class OldValue implements Serializable{
        private static final long serialVersionUID = 1L;
        private final Object value;
        private final Date dateModified;
        private final String user;

        public OldValue( Object value, Date dateModified, String user ) {
            this.value = value;
            this.dateModified = dateModified;
            this.user = user;
        }

        public Date getDateModified() {
            return dateModified;
        }

        public String getUser() {
            return user;
        }

        public Object getValue() {
            return value;
        }
    }
}
