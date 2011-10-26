
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.search.BaseResourceIndexer;
import com.ettrema.console.Result;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.grid.LocalAsynchProcessor;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.List;
import java.util.UUID;

public class Crawl extends AbstractConsoleCommand{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Crawl.class);

    private static final long serialVersionUID = 1L;
    
    Crawl(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    public Result execute() {
        log.debug("enqueueing host crawl");
        Resource cur = currentResource();
        if( !(cur instanceof CommonTemplated) ) {
            return result("Resource is not compatible: " + cur.getClass());
        }
        CommonTemplated ct = (CommonTemplated) cur;
        Host h = ct.getHost();
        HostCrawler crawler = new HostCrawler(h.getNameNodeId());
        RequestContext context = RequestContext.getCurrent();
        AsynchProcessor proc = context.get(AsynchProcessor.class);
        proc.enqueue(crawler);
        log.debug("done enqueueing host crawl");
        
        return result("submitted crawl job");
    }

    public static class HostCrawler implements Processable {
        private static final long serialVersionUID = 5405320305282298433L;
        final UUID hostNodeId;

        public HostCrawler(UUID hostNodeId) {
            this.hostNodeId = hostNodeId;
        }                
        
        @Override
        public void doProcess(Context context) {
            VfsSession session = context.get(VfsSession.class);
            NameNode nHost = session.get(hostNodeId);
            if( nHost == null ) {
                log.error("Name node for host does not exist: " + hostNodeId);
                return ;
            }
            Object data = nHost.getData();
            if( data == null ) {
                log.error("Data node does not exist. Name node: " + hostNodeId);
                return ;
            }
            if( !(data instanceof Host) ) {
                log.error("Node does not reference a Host. Instead references a: " + data.getClass() + " ID:" + hostNodeId);
                return ;
            }
            
            Host h = (Host)data;
            
            LocalAsynchProcessor proc = context.get(LocalAsynchProcessor.class);
            crawl(h, proc);
        }

        private void crawl(BaseResource res, LocalAsynchProcessor proc) {
            log.debug("crawl: " + res.getHref());
            BaseResourceIndexer indexer = new BaseResourceIndexer(res.getNameNodeId());
            proc.enqueue(indexer);
            if( res instanceof Folder ) {
                Folder f = (Folder)res;
                for( Resource r : f.getChildren() ) {
                    if( r instanceof BaseResource ) {
                        crawl((BaseResource) r,proc);
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "Crawler: " + hostNodeId;
        }

        public void pleaseImplementSerializable() {
            
        }

        
    }
}
