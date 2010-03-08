
package com.bradmcevoy.vfs;

import com.bradmcevoy.context.RequestContext;

public abstract class VfsCommon {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VfsCommon.class);
    
    public VfsCommon() {
    }

    protected VfsSession vfs() {
        VfsSession vfs = requestContext().get(VfsSession.class);
        if( vfs == null ) throw new RuntimeException("Vfs is not registered with context. Add <factory class='com.bradmcevoy.vfs.CtxVfsFactory' /> to see catalog.xml");
        return vfs;
    }
    
    protected RequestContext requestContext() {
        return RequestContext.getCurrent();
    }    
    
    public void commit() {
        log.debug("committing");
        vfs().commit();
    }

    public void rollback() {
//        log.debug("rolling back");
        vfs().rollback();
    }
    

}
