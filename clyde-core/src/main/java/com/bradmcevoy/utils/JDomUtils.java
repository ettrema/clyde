package com.bradmcevoy.utils;

import com.bradmcevoy.xml.XmlHelper;
import java.util.ArrayList;
import java.util.List;
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
        return childrenOf( e2, name, Namespace.NO_NAMESPACE );
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
     * @param NS
     * @return
     */
    public static String valueOf( Element el, String name, Namespace NS ) {
        Element elChild = el.getChild( name, NS );
        if( elChild == null ) {
            return null;
        } else {
            return getInnerXml( elChild );
        }
    }

    public static String getInnerXml( Element el ) {
        String v = XmlHelper.getAllText( el );
        if( v == null ) return null;
        return v.trim();
    }

    public static void setInnerXml(Element el, String xml) {
        XmlHelper helper = new XmlHelper();
        List content = helper.getContent( xml );
        el.setContent( content );
    }

    public static void setChildText( Element el, String childName, String val, Namespace ns ) {
        if( val != null ) {
            Element elScript = new Element( childName, ns );
            elScript.setText( val );
            el.addContent( elScript );
        }
    }

    public static void setChildXml( Element el, String childName, String val, Namespace ns ) {
        if( val != null ) {
            Element elChild = new Element( childName, ns );
            el.addContent( elChild );
            setInnerXml( elChild, val );
        } else {
            System.out.println( "no val: " + childName );
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
