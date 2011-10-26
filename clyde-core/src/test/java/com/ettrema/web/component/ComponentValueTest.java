package com.bradmcevoy.web.component;

import java.util.List;
import junit.framework.TestCase;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class ComponentValueTest extends TestCase {

    ComponentValue cv;

    public ComponentValueTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        cv = new ComponentValue( "test", null );
        cv.setValue( "abc");
    }

    public void test() {
        
    }
//
//    public void testToXml_Simpletext() {
//        List l = cv.formatContentToXmlList( "abc" );
//        System.out.println( "testToXml_Simpletext" );
//        for( Object o : l ) {
//            System.out.println( o );
//        }
//    }
//
//    public void testToXml_TextThenElement() {
//        List l = cv.formatContentToXmlList( "abc<a href='xx'>hi there</a>&copy;" );
//        System.out.println( "testToXml_TextThenElement" );
//        for( Object o : l ) {
//            if( o instanceof Element ) {
//                Element el = (Element) o;
//                System.out.println( "el: " + el.getName() + " atts:" + el.getAttributes().size() );
//            } else {
//                System.out.println( o );
//            }
//        }
//    }

}
