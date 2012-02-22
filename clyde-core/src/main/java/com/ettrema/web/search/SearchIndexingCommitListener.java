package com.ettrema.web.search;

import com.ettrema.common.Service;
import com.ettrema.web.BaseResource;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.vfs.CommitListener;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsProvider;

/**
 * After commit, this listener creates an indexing job for the given name node 
 * and submits it to the asynch processing service
 * 
 * @author brad
 */
public class SearchIndexingCommitListener implements CommitListener, Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchIndexingCommitListener.class);
    private final VfsProvider vfsProvider;
    private final AsynchProcessor proc;

    public SearchIndexingCommitListener(VfsProvider vfsProvider, AsynchProcessor proc) {
        this.vfsProvider = vfsProvider;
        this.proc = proc;
    }


    @Override
    public void onCommit(NameNode n) throws Exception {
        if( n.getData() instanceof BaseResource){
            log.debug("onCommit: " + n.getName());
            if (proc != null) {
                proc.enqueue(new BaseResourceIndexer(n.getId()));
            } else {
                log.warn("No LocalAsynchProcessor configured, so not indexing");
            }
        }
    }

    @Override
    public void start() {
        vfsProvider.addCommitListener(this);
    }

    @Override
    public void stop() {
        vfsProvider.removeCommitListener(this);
    }

}
