package com.ettrema.web;

import com.ettrema.web.Page;
import com.ettrema.web.RootFolder;
import com.ettrema.web.component.ComponentValue;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class BaseResourceListTest {

    RootFolder root;

    public BaseResourceListTest() {
        root = new RootFolder();
    }

    @Test
    public void testGroupByField() {
//        BaseResourceList list = new BaseResourceList();
//        list.add( createPage( "page1","group1" ) );
//
//        list.add( createPage( "page4","group2" ) );
//        list.add( createPage( "page5","group2" ) );
//
//        list.add( createPage( "page7","group3" ) );
//        list.add( createPage( "page8","group3" ) );
//        list.add( createPage( "page9","group3" ) );
//        Map<Object, BaseResourceList> groups = list.groupByField( "param" );
//        assertEquals( 3, groups.size());
//
//        assertEquals( 1, groups.get("group1").size());
//        assertEquals( 2, groups.get("group2").size());
//        assertEquals( 3, groups.get("group3").size());

    }

    private Page createPage( String name, String paramVal ) {
        Page p = new Page( root, name );
        ComponentValue cv = new ComponentValue( "param", p );
        cv.setValue( paramVal );
        p.getValues().add( cv );
        return p;
    }
}
