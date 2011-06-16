/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bradmcevoy.web.component;

import junit.framework.TestCase;
import org.jdom.CDATA;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class InitUtilsTest extends TestCase {

    public InitUtilsTest( String testName ) {
        super( testName );
    }

    public void testGetBoolean() {
    }

    public void testGetInt() {
    }

    public void testGetInteger() {
    }

    public void testGetPath() {
    }

    public void testInit() {
    }

    public void testSet_3args_1() {
    }

    public void testSet_3args_2() {
    }

    public void testSet_3args_3() {
    }

    public void testSet_3args_4() {
    }

    public void testSetBoolean() {
    }

    public void testGetValue_3args() {
    }

    public void testGetValue_Element_String() {
    }

    public void testSetString_Element_AbstractInput() {
    }

    public void testSetString_3args() {
    }

    public void testToXml() {
    }

    public void testComponentFieldsToXml() {
    }

    public void testGetList() {
    }

    public void testGetBigDecimal() {
    }

    public void testSetElementString() {
    }

    public void testGetElementValue() {
        Element elRoot = new Element( "root" );
        Element el = new Element( "theElement" );
        elRoot.addContent( el );
        org.jdom.Text t = new org.jdom.Text( "abc" );
        el.addContent( t );
        Element elChild = new Element( "br" );
        el.addContent( elChild );
        CDATA cd = new CDATA( "def>><<" );
        el.addContent( cd );

        String s = InitUtils.getElementValue( elRoot, "theElement" );
        assertEquals( "abc<br />def>><<", s);
    }
}
