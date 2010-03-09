package com.bradmcevoy.ant;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import org.apache.tools.ant.BuildException;

public class ClydeDeleteTask extends AbstractClydeTask {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeDeleteTask.class);
        
    public ClydeDeleteTask() {
        super();
        System.out.println("creating clyde delete");
    }
        
    void execute(Context context) throws BuildException {
        try {
            System.out.println("delete-execute");
            log.debug("delete task: execute");
            if( context == null ) throw new NullPointerException("context");
            final Path p = Path.path(path);
            VfsSession sess = context.get(VfsSession.class);
            if( p.getName().equals("*")) {
                NameNode n = sess.find(p.getParent());
                if( n == null ) {
                    log.warn("node not found: " + p.getParent());
                    return;
                }
                for( NameNode nChild : n.children() ) {
                    delete(nChild);
                }
            } else {
                NameNode n = sess.find(p);
                if( n == null ) {
                    log.warn("node not found: " + p);
                    return;
                }
                log.debug("deleting: " + n.getId());
                delete(n);
                
            }
            sess.commit();
        } catch (Throwable ex) {
            ex.printStackTrace();
            log.error("exception doing delete",ex);
            throw new BuildException(ex);
        }
    }   
    
    void delete(NameNode n) {
        n.delete();
    }
}
