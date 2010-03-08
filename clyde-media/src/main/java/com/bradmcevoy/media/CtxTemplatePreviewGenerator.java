
package com.bradmcevoy.media;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Factory;
import com.bradmcevoy.context.Registration;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.grid.AsynchProcessor;
import com.bradmcevoy.vfs.CommitListener;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsProvider;
import com.bradmcevoy.web.Template;

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
