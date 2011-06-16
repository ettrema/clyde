package com.bradmcevoy.web;

import com.bradmcevoy.AbstractTest;
import com.bradmcevoy.TestUtils;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;

public class TestLookup extends AbstractTest{
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestLookup.class);
    
    public TestLookup() {
        super("testtemplating");
    }
    
    public void test() {
        TestUtils.runTest( new Executable() { public Object execute(Context context) {
//            doTest2(context);
//            doTest1(context);
            return null;
        } });
    }
    
        
    private void doTest2(final Context context) {
        VfsSession sess = context.get(VfsSession.class);
        NameNode n = sess.root();
        show(n,"");
    }

    private void show(NameNode n, String pad) {
        log.debug(pad + n.getName() + " :: " + n.getDataClass());
        for( NameNode child : n.children() ) {
            show(child,pad + "   ");
        }
    }
}
