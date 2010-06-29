package com.bradmcevoy.web.search;

import com.bradmcevoy.context.Registration;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Factory;
import com.bradmcevoy.grid.AsynchProcessor;
import com.bradmcevoy.vfs.CommitListener;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsProvider;

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
        log.debug("onCommit: " + this.hashCode());
        AsynchProcessor proc = rootContext.get(AsynchProcessor.class);
        if (proc != null) {
            proc.enqueue(new BaseResourceIndexer(n.getId()));
        } else {
            log.warn("No LocalAsynchProcessor configured, so not indexing");
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
