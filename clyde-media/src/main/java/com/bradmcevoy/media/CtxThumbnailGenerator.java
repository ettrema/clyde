package com.bradmcevoy.media;

import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Factory;
import com.bradmcevoy.context.Registration;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.grid.AsynchProcessor;
import com.bradmcevoy.grid.Processable;
import com.bradmcevoy.vfs.CommitListener;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsProvider;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.Thumb;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * On startup, registers self as a commitlistener. On commits of imagefiles
 * it creates an asynch job to generate thumbnails
 * 
 * @author brad
 */
public class CtxThumbnailGenerator implements Factory<Object>, CommitListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CtxThumbnailGenerator.class);
    private static final long serialVersionUID = 1L;
    public static Class[] classes = {};

    public CtxThumbnailGenerator() {
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
        log.info("Starting thumbnail generator..");
        VfsProvider vfsProvider = context.get(VfsProvider.class);
        vfsProvider.addCommitListener(this);
    }

    public void destroy() {
    }

    public void onRemove(Object item) {
    }

    public void onCommit(NameNode n) throws Exception {
        DataNode dn = n.getData();
        if (dn instanceof ImageFile) {
            RequestContext context = RequestContext.getCurrent();
            ImageFile f = (ImageFile) dn;
            if( f.getParentFolder() != null ) {
                List<Thumb> thumbSpecs = Thumb.getThumbSpecs( f.getParentFolder());
                if( thumbSpecs == null || thumbSpecs.size() == 0 ) return;
                ThumbnailGeneratorProcessable proc = new ThumbnailGeneratorProcessable(n.getId(), n.getName());
                AsynchProcessor asynchProc = context.get(AsynchProcessor.class);
                asynchProc.enqueue(proc);
            } else {
                log.warn("image has no parent folder! " + f.getName());
            }
        }
    }

    public static class ThumbnailGeneratorProcessable implements Processable, Serializable {

        private static final long serialVersionUID = 1L;
        final String targetName;
        final UUID imageFileNameNodeId;

        public ThumbnailGeneratorProcessable(UUID imageFileNameNodeId, String name) {
            this.targetName = name;
            this.imageFileNameNodeId = imageFileNameNodeId;
        }

        public void doProcess(Context context) {
            log.debug("generating thumbs: " + targetName + "...");
            VfsSession vfs = context.get(VfsSession.class);
            NameNode pageNameNode = vfs.get(imageFileNameNodeId);
            if( pageNameNode == null ) {
                log.debug("..name node not found. prolly deleted: " + targetName);
                return ;
            }
            DataNode dn = pageNameNode.getData();
            if (dn == null) {
                log.warn("Could not find target: " + imageFileNameNodeId);
                return;
            }
            ImageFile targetPage;
            if (dn instanceof ImageFile) {
                targetPage = (ImageFile) dn;
            } else {
                log.warn("Target page is not of type CommonTemplated. Is a: " + dn.getClass().getName());
                return;
            }
            try {
                int count = generate(targetPage);
                if( count > 0 ) {
                    vfs.commit();
                } else {
                    vfs.rollback();
                }
            } catch(Exception e) {
                // consume exception so we don't keep trying to process same message
                log.error( "failed to generate thumbs for: " + targetPage.getHref(), e);
                vfs.rollback();
            }
        }

        /**
         * 
         * @param targetPage
         * @return - number of thumbs generated
         */
        private int generate(ImageFile targetPage) {
            log.debug("...doing generation...");
            return targetPage.generateThumbs();
        }

        public void pleaseImplementSerializable() {
        }
    }
}
