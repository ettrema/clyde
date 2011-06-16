package com.bradmcevoy;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.RequestContext;
import junit.framework.TestCase;

public class AbstractTest extends TestCase {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractTest.class);
    public AbstractTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        log.debug("setup");
        TestUtils.setup();
    }
    
    protected void tearDown() throws Exception {
        log.debug("teardown");
        TestUtils.tearDown();
    }
    
    protected void debug(Object o) {        
        if( o == null ) System.out.println("");
        else System.out.println(o);
    }        
 
    protected Context ctx() {
        return RequestContext.getCurrent();
    }   
}
