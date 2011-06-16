package com.bradmcevoy.ant;

import com.ettrema.context.Context;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.File;
import org.apache.tools.ant.BuildException;

public class ClydeReportTask extends AbstractClydeTask {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeExportTask.class);
    
    private File folder;
    
    private boolean reportOnly;
    
    public ClydeReportTask() {
    }
    
    void execute(Context context) throws BuildException {
        try {
            VfsSession sess = context.get(VfsSession.class);
            NameNode n = sess.root();
            show(n,"");            
        } catch (Throwable ex) {
            ex.printStackTrace();
            log.error("exception doing export",ex);
            throw new RuntimeException(ex);
        }
    }
    
    private void show(NameNode n, String pad) {
        String s = pad + n.getName() + " :: " + n.getDataClass();
        log.debug(s);
        System.out.println(s);
        for( NameNode child : n.children() ) {
            show(child,pad + "   ");
        }
    }    
}
