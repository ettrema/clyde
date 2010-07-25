
package com.bradmcevoy.media;

import com.bradmcevoy.web.Template;
import com.ettrema.context.Context;
import com.ettrema.context.Factory;
import com.ettrema.context.Registration;
import com.ettrema.context.RequestContext;
import com.ettrema.context.RootContext;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.vfs.CommitListener;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsProvider;

/**
 * Factory which isnt really a factory. On startup, registers itself as a CommitListener
 * on VFS
 * 
 * Each committed namenode is checked to see if its a template, and if so a job
 * is queued to generate its preview
 * 
 * @author brad
 */
public class CtxTemplatePreviewGenerator implements Factory<Object>, CommitListener {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CtxTemplatePreviewGenerator.class);
    public static Class[] classes = {};
    
    
    public CtxTemplatePreviewGenerator() {
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

    public void init(RootContext context) {
        log.info("Starting template preview generator..");
        VfsProvider vfsProvider = context.get(VfsProvider.class);
        vfsProvider.addCommitListener( this );
    }

    public void destroy() {
    }

    public void onRemove(Object item) {
        
    }

    public void onCommit(NameNode n) throws Exception {
        DataNode dn = n.getData();
        if( dn instanceof Template ) {
            RequestContext context = RequestContext.getCurrent();
            Template t = (Template) dn;
            PageImageRenderer imageRenderer = new PageImageRenderer(n.getId(), n.getName());
            AsynchProcessor asynchProc = context.get(AsynchProcessor.class);
            asynchProc.enqueue(imageRenderer);
        }
    }
    
}
