package com.ettrema.web.error;

import com.ettrema.web.error.HtmlExceptionFormatter;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class HtmlExceptionFormatterTest extends TestCase {

    HtmlExceptionFormatter formatter;

    public HtmlExceptionFormatterTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        formatter = new HtmlExceptionFormatter();
    }

    public void testFormatExceptionAsHtml_SingleException() {
        Exception e = new Exception( "exception123" );
        String s = formatter.formatExceptionAsHtml( e );
        System.out.println( "SingleEx" );
        System.out.println( s );
        assertTrue( s.contains( "<html>" ) );
        assertTrue( s.contains( "<body>" ) );
        assertTrue( s.contains( "exception123" ) );
        assertTrue( s.contains( "</html>" ) );
    }

    public void testFormatExceptionAsHtml_CausedByException() {
        Exception e1 = new Exception( "exception1" );
        Exception e2 = new Exception( "exception2",e1 );
        String s = formatter.formatExceptionAsHtml( e2 );
        System.out.println( "CausedBy" );
        System.out.println( s );
        assertTrue( s.contains( "<html>" ) );
        assertTrue( s.contains( "<body>" ) );
        assertTrue( s.contains( "exception1" ) );
        assertTrue( s.contains( "exception2" ) );
        assertTrue( s.contains( "</html>" ) );
    }

}
