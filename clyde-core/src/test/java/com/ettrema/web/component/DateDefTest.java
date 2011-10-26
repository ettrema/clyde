/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ettrema.web.component;

import com.ettrema.web.component.DateDef;
import java.text.ParseException;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author HP
 */
public class DateDefTest {
    
    public DateDefTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    
    
    
    @Test
    public void testSomeMethod() throws ParseException {
        DateDef def = new DateDef(null, "x");
        def.setShowTime(false);
        Date dt = def.parseValue(null, null, "29/03/2011");
        assertNotNull(dt);
        dt = def.parseValue(null, null, "29/03/2011 10:20");
        assertNotNull(dt);
        
        def.setShowTime(true);
        dt = def.parseValue(null, null, "29/03/2011");
        assertNotNull(dt);
        dt = def.parseValue(null, null, "29/03/2011 10:20");
        assertNotNull(dt);
    }
}
