package com.ettrema.utils;

import com.bradmcevoy.xml.XmlHelper;
import java.util.ArrayList;
import java.util.List;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class JDomUtils {

    public static List<org.jdom.Element> children( org.jdom.Element e2 ) {
        List list = e2.getChildren();
        List<org.jdom.Element> els = new ArrayList<org.jdom.Element>();
        for( Object o : list ) {
            els.add( (org.jdom.Element) o );
        }
        return els;
    }

    public static List<org.jdom.Element> childrenOf( org.jdom.Element e2, String name ) {
        Element elNext = e2.getChild( name );
        List<org.jdom.Element> els = new ArrayList<org.jdom.Element>();
        if( elNext != null ) {
            List list = elNext.getChildren();
            for( Object o : list ) {
                if( o instanceof Element ) {
                    els.add( (org.jdom.Element) o );
                }
            }
        }
        return els;
    }

    public static List<org.jdom.Element> childrenOf( org.jdom.Element e2, String name, Namespace ns ) {
        Element elNext = e2.getChild( name, ns );
        List<org.jdom.Element> els = new ArrayList<org.jdom.Element>();
        if( elNext != null ) {
            List list = elNext.getChildren();
            for( Object o : list ) {
                if( o instanceof Element ) {
                    els.add( (org.jdom.Element) o );
                }
            }
        }
        return els;
    }

    /**
     *
     * Get the text content of the named child element
     *
     * @param el
     * @param name
     * @param ns
     * @return
     */
    public static String valueOf( Element el, String name, Namespace ns ) {
        Element elChild = el.getChild( name, ns );
        if( elChild == null ) {
            return null;
        } else {
            return getInnerXml( elChild );
        }
    }


    public static String getInnerXmlOf( Element parent, String elementName ) {
        Element el = getChild( parent, elementName );
        if( el == null ) {
            return null;
        } else {
            return getInnerXml( el );
        }
    }

    public static String getInnerXmlOf( Element parent, String elementName, Namespace NS ) {
        Element el = parent.getChild( elementName, NS );
        if( el == null ) {
            return null;
        } else {
            return getInnerXml( el );
        }
    }

    public static String getInnerXml( Element el ) {
        String v = XmlHelper.getAllText( el );
        if( v == null ) return null;
        return v.trim();
    }

    public static void setInnerXml(Element el, String xml) {
        List content = XmlHelper.getContent( xml );
        el.setContent( content );
    }

    /**
     * Returns the entire element as formatted XML, including the given element itself
     * @param el
     * @return
     */
    public static String getXml(Element el) {
        return XmlHelper.toString( el );
    }

    /**
     * Used for values which are not necessarily xml structures, so might contain
     * invalid xml data. So wraps in a CDATA element.
     *
     * Eg might produce
     *
     * <script><![CDATA[
     * if( a && b > x ) {
     * }
     * ]]></script>
     *
     * @param el
     * @param childName
     * @param val
     * @param ns
     */
    public static void setChildText( Element el, String childName, String val, Namespace ns ) {
        if( val != null ) {
            Element elScript = new Element( childName, ns );
            CDATA cdata = new CDATA( val );
            elScript.addContent( cdata );
            el.addContent( elScript );
        }
    }

    public static void setChildXml( Element el, String childName, String val, Namespace ns ) {
        if( val != null ) {
            Element elChild = new Element( childName, ns );
            el.addContent( elChild );
            setInnerXml( elChild, val );
        } else {

        }
    }


    /**
     * Returns the first child of any namespace with the given local name
     * @param el
     * @param name
     */
    public static Element getChild(Element el, String name) {
        for( Object o : el.getChildren() ) {
            if( o instanceof Element ) {
                Element elChild = (Element) o;
                if( elChild.getName().equals( name )) {
                    return elChild;
                }
            }
        }
        return null;
    }
}
