package com.bradmcevoy.web.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class ImageUtilitiesTest {
    @Test
    public void testRead() throws IOException {
        InputStream in = this.getClass().getResourceAsStream( "/demo.png");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageUtilities.scaleProportionallyWithMax( in, out, 100, 100, "jpeg");
        assertEquals( 1968, out.toByteArray().length);
    }

}