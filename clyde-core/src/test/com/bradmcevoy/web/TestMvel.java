package com.bradmcevoy.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.mvel.TemplateInterpreter;

public class TestMvel extends TestCase{
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestLookup.class);
    
    public TestMvel() {
        super("testtemplating");
    }
    
    /**
     * This fails because there is an @ symbol in the template which is
     *  interpreted as denoting an expression, when in fact its just part
     *  of content. It is not connected to curly braces
     * 
     * @throws java.io.IOException
     */
    public void test_fails() throws IOException {
        runTemplate("problemtemplate.txt");
    }

    /**
     * This template is the same as above except that the @ symbol has been replaced
     * with ' at '. It succeeds.
     * 
     * @throws java.io.IOException
     */
    public void test_works() throws IOException {
        runTemplate("oktemplate.txt");
    }
    
    public static ByteArrayOutputStream readIn(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int i = is.read();
        while( i >= 0 ) {
            os.write( i );
            i = is.read();
        }
        return os;
    }

    private void runTemplate(String file) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(file);        
        String template = readIn(is).toString();
        Map map = new HashMap();
        map.put("someval", "some value goes here");
        String r = TemplateInterpreter.evalToString(template,map);
        System.out.println("r: " + r);        
    }
    
}
