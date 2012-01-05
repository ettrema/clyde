/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bradmcevoy.utils;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 *
 * @author brad
 */
public class ClydeUtilsTest extends TestCase {

    public ClydeUtilsTest( String testName ) {
        super( testName );
    }

    public void testGetDateAsName() {
        String s = ClydeUtils.getDateAsName();
        assertNotNull( s );
        System.out.println( "getDateAsName: " + s );
    }

    public void testGetDateAsNameUnique_NoOtherFiles() {
        String s = ClydeUtils.getDateAsName();
        CollectionResource col = createMock( CollectionResource.class );
        expect( col.child( (String) anyObject() ) ).andReturn( null );
        replay( col );
        String s2 = ClydeUtils.getDateAsNameUnique( col );
        assertEquals( s, s2 );
    }

    public void testGetDateAsNameUnique_Conflict() {
        String s = ClydeUtils.getDateAsName();
        CollectionResource col = createMock( CollectionResource.class );
        Resource r = createMock( Resource.class );
        expect( col.child( s )).andReturn( r );
        expect( col.child( s+"(1)" ) ).andReturn( null );
        replay( col );
        String s2 = ClydeUtils.getDateAsNameUnique( col );
        assertEquals( s + "(1)", s2 );
    }
}
