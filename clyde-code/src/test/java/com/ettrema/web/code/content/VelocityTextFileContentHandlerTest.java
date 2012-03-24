package com.ettrema.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class VelocityTextFileContentHandlerTest {
    
    private String defaultCharset = "UTF-8";
    
    @Before
    public void setUp() {
    }

    @Test
    public void testCharset_Null() {
        assertEquals(defaultCharset, VelocityTextFileContentHandler.getCharset(null, defaultCharset));
    }

    @Test
    public void testCharset_Blank() {
        assertEquals(defaultCharset, VelocityTextFileContentHandler.getCharset("", defaultCharset));
    }

    @Test
    public void testCharset_NoCharset() {
        assertEquals(defaultCharset, VelocityTextFileContentHandler.getCharset("text/html", defaultCharset));
    }

    @Test
    public void testCharset_EmptyCharset() {
        assertEquals(defaultCharset, VelocityTextFileContentHandler.getCharset("text/html; charset=", defaultCharset));
    }
    
    @Test
    public void testCharset_WhitespaceCharset() {
        assertEquals(defaultCharset, VelocityTextFileContentHandler.getCharset("text/html; charset= ", defaultCharset));
    }    
    
    @Test
    public void testCharset_HappyDay() {
        assertEquals("ISO-123", VelocityTextFileContentHandler.getCharset("text/html; charset=ISO-123", defaultCharset));
    }    
    
}
