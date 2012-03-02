package com.ettrema.vfs;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class VfsTransactionManager {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VfsTransactionManager.class);
    private static String ATT_ROLLBACK_ONLY = "_clyde_rollbackonly";
    
    public static void commit() {
        log.debug("committing");
        VfsSession vfs = _(VfsSession.class);
        Boolean rollbackOnly = (Boolean) vfs.attributes.get(ATT_ROLLBACK_ONLY);
        if(rollbackOnly != null && rollbackOnly) {
            log.trace("ignore trace because rollback only is set");
        } else {
            _(VfsSession.class).commit();
        }
    }

    public static void rollback() {
        log.trace("rolling back");
        setRollbackOnly(false);
        _(VfsSession.class).rollback();
    }    
    
    public static void setRollbackOnly(Boolean on) {
        _(VfsSession.class).attributes.put(ATT_ROLLBACK_ONLY, on);
    }
}
