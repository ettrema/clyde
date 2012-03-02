
package com.ettrema.vfs;

import com.ettrema.context.RequestContext;


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
        VfsTransactionManager.commit();
    }

    public void rollback() {
//        log.debug("rolling back");
        VfsTransactionManager.rollback();
    }
    
    

}
