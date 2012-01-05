/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bradmcevoy.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class ReplaceableHtmlParserImplTest extends TestCase {

    ReplaceableHtmlParserImpl parser;

    public ReplaceableHtmlParserImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        parser = new ReplaceableHtmlParserImpl();
    }

    public void testAddMarkers() {
        String markedUp = parser.addMarkers("abc", "aName");
        System.out.println(markedUp);
        Collection<String> names = new HashSet<String>();
        names.add("aName");
        Map<String, String> map = parser.parse(markedUp, names);
        assertEquals("abc", map.get("aName"));
    }



}
