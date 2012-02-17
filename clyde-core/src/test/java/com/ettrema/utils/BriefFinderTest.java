package com.ettrema.utils;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class BriefFinderTest {


    @Before
    public void setUp() {

    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testFindBrief_HappyDayCase() {
        String result = BriefFinder.findBrief("<p>abc</p>ddd", 5);
        assertEquals("abc", result);
    }    
    
    /**
     * Test of findBrief method, of class BriefFinder.
     */
    @Test
    public void testFindBrief_Long() {
        String result = BriefFinder.findBrief("<h1>xxx</h1><p>yyy</p>ddd", 3);
        assertEquals("yyy", result);
    }
    
    @Test
    public void testFindBrief_Short() {
        String result = BriefFinder.findBrief("<h1>xxx</h1><p>abc</p>ddd", 2);
        assertEquals("ab", result);
    }    
    
    @Test
    public void testFindBrief_NoCloseTag() {
        String result = BriefFinder.findBrief("<h1>xxx</h1><p>abcddd", 2);
        assertEquals("ab", result);
    }        
    
    @Test
    public void testFindBrief_NoMarkup() {
        String result = BriefFinder.findBrief("abcd<h1>yyy</h1>ddd", 3);
        assertEquals("abc", result);
    }    
    @Test
    public void testFindBrief_NoBrief() {
        String result = BriefFinder.findBrief("<h1>yyy</h1>ddd", 3);
        assertNull(result);
    }        
    
    
}
