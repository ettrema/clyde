
package com.bradmcevoy.web;

import com.bradmcevoy.grid.LocalAsynchProcessor;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.vfs.CommitListener;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.web.search.BaseResourceIndexer;

/**
 * After commit, this listener creates an indexing job for the given name node 
 * and submits it to the asynch processing service
 * 
 * @author brad
 */
public class VfsCommitListener implements CommitListener{
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VfsCommitListener.class);
    
    final private Context context;
    
    public VfsCommitListener(Context context) {
        log.debug("Created VfsCommitListener");
        this.context = context;
    }

    @Override
    public void onCommit(NameNode n) throws Exception {
//        log.debug("onCommit: " + this.hashCode());
        LocalAsynchProcessor proc = context.get(LocalAsynchProcessor.class);
        if( proc != null ) {
            proc.enqueue(new BaseResourceIndexer(n.getId()));
        } else {
            log.warn("No LocalAsynchProcessor configured, so not indexing");
        }

    }



}
