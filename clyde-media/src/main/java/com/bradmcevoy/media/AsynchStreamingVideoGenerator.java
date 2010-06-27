package com.bradmcevoy.media;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Factory;
import com.bradmcevoy.context.Registration;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.vfs.CommitListener;
import com.bradmcevoy.grid.AsynchProcessor;
import com.bradmcevoy.grid.Processable;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.vfs.VfsProvider;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.VideoFile;
import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class AsynchStreamingVideoGenerator extends VfsCommon implements Factory<Object>, CommitListener  {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( AsynchStreamingVideoGenerator.class );

    public Class[] keyClasses() {
        return null;
    }

    public String[] keyIds() {
        return null;
    }

    public Registration<Object> insert(RootContext context, Context requestContext) {
        return null;
    }

    public void init(RootContext context) {
        log.info("Hello from the AsynchStreamingVideoGenerator. I'll convert video files to streaming format");
        VfsProvider vfsProvider = context.get(VfsProvider.class);
        vfsProvider.addCommitListener(this);
    }

    public void destroy() {
    }

    public void onRemove(Object item) {
    }

    public void onCommit(NameNode n) throws Exception {
        DataNode dn = n.getData();
        if (dn instanceof VideoFile) {            
            VideoFile source = (VideoFile) dn;
            if( !source.isTrash()) {
                enqueueGenerateStreamingVideo( source );
            } else {
                log.debug( "not generating as in trash folder");
            }
        }
    }

    private void enqueueGenerateStreamingVideo( VideoFile source ) {
        log.debug( "generateStreamingVideo: " + source.getName());
        final UUID id = source.getNameNodeId();
        final String sourceName = source.getName();
        Processable proc = new StreamingVideoProcessable( sourceName, id );
        AsynchProcessor processor = RequestContext.getCurrent().get( AsynchProcessor.class );
        processor.enqueue( proc );
    }

    public static class StreamingVideoProcessable extends VfsCommon implements Processable, Serializable{

        private static final long serialVersionUID = 1L;

        private final String sourceName;
        private final UUID id;

        public StreamingVideoProcessable( String sourceName, UUID id ) {
            this.sourceName = sourceName;
            this.id = id;
        }


        public void doProcess( Context context ) {
                log.debug( "processing: " + sourceName);
                VfsSession vfs = context.get( VfsSession.class );
                NameNode nn = vfs.get( id );
                if( nn == null ) {
                    log.warn( "Couldnt find node: " + id );
                    return;
                }
                DataNode data = nn.getData();
                if( data == null ) {
                    log.warn( "node was found but datanode was null: name node id: " + id );
                } else if( data instanceof VideoFile ) {
                    VideoFile file = (VideoFile) data;
                    SynchronousStreamingVideoGenerator gen = new SynchronousStreamingVideoGenerator();
                    try {
                        gen.generateStreamingVideo( file );
                        commit();
                    } catch( Exception e ) {
                        log.warn("Exception generating streaming video: " + file.getHref(), e);
                        rollback();
                    }
                } else {
                    log.warn( "Not an instanceof video file: " + data.getClass() );
                }
        }

        public void pleaseImplementSerializable() {
        }

    }
}
