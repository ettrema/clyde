package com.bradmcevoy.web.search;

import com.bradmcevoy.web.BaseResource;
import com.ettrema.context.Context;
import com.ettrema.context.Factory;
import com.ettrema.context.Registration;
import com.ettrema.context.RootContext;
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
public class SearchIndexingCommitListener implements Factory<Object>, CommitListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchIndexingCommitListener.class);
    public static Class[] classes = {};
    private RootContext rootContext;

    @Override
    public void onCommit(NameNode n) throws Exception {
        if( n.getData() instanceof BaseResource){
            log.debug("onCommit: " + n.getName());
            AsynchProcessor proc = rootContext.get(AsynchProcessor.class);
            if (proc != null) {
                proc.enqueue(new BaseResourceIndexer(n.getId()));
            } else {
                log.warn("No LocalAsynchProcessor configured, so not indexing");
            }
        }
    }

    public Class[] keyClasses() {
        return classes;
    }

    public String[] keyIds() {
        return null;
    }

    public Registration<Object> insert(RootContext context, Context requestContext) {
        return null;
    }

    public void init(RootContext rootContext) {
        this.rootContext = rootContext;
        VfsProvider vfsProvider = rootContext.get(VfsProvider.class);
        vfsProvider.addCommitListener(this);
    }

    public void destroy() {
    }

    public void onRemove(Object item) {
    }
}
