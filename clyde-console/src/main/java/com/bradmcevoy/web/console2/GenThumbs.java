package com.bradmcevoy.web.console2;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.grid.LocalAsynchProcessor;
import com.bradmcevoy.grid.Processable;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ImageFile;
import com.ettrema.console.Result;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class GenThumbs extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( GenThumbs.class );

    GenThumbs( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        Resource r = currentResource();
        Folder f = (Folder) r;
        LocalAsynchProcessor proc = RequestContext.getCurrent().get(LocalAsynchProcessor.class);
        int files = crawl(f, proc);

        return result( "Processing image files: " + files );
    }

    private int crawl(Folder f, LocalAsynchProcessor proc) {
        log.debug("crawl: " + f.getHref());
        int cnt = 0;
        for( Resource r : f.getChildren() ) {
            if( r instanceof ImageFile ) {
                ImageFile file = (ImageFile) r;
                ThumbGenerator gen = new ThumbGenerator( file.getNameNodeId());
                proc.enqueue( gen );
                cnt++;
            }
            if( r instanceof Folder) {
                cnt += crawl((Folder) r,proc);
            }
        }
        return cnt;
    }

    public static class ThumbGenerator extends VfsCommon implements Processable {
        final UUID folderId;

        private static final long serialVersionUID = 1L;

        public ThumbGenerator(UUID folderId) {
            this.folderId = folderId;
        }

        @Override
        public void doProcess(Context context) {
            VfsSession session = context.get(VfsSession.class);
            NameNode nHost = session.get(folderId);
            if( nHost == null ) {
                log.error("Name node for host does not exist: " + folderId);
                return ;
            }
            Object data = nHost.getData();
            if( data == null ) {
                log.error("Data node does not exist. Name node: " + folderId);
                return ;
            }
            if( !(data instanceof ImageFile) ) {
                log.error("Node does not reference a Folder. Instead references a: " + data.getClass() + " ID:" + folderId);
                return ;
            }

            ImageFile file = (ImageFile) data;
            file.generateThumbs();
            commit();
        }


        @Override
        public String toString() {
            return "Crawler: " + folderId;
        }

        public void pleaseImplementSerializable() {
            
        }
    }

}

