package com.bradmcevoy.web;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable2;
import com.bradmcevoy.context.FactoryCatalog;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.vfs.MemoryVfsProvider;
import junit.framework.TestCase;

public class TestTemplating2 extends TestCase {
    
    private RootContext rootContext;
    
    public TestTemplating2() {
    }

    protected void setUp() throws Exception {
        rootContext = new RootContext();
        rootContext.put( new MemoryVfsProvider() );
    }
    

    public void test() {
        rootContext.execute( new Executable2() {
            public void execute(Context context) {

            }
        });
    }
}
