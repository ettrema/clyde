package com.ettrema.web.console2;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.BaseResource;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.binary.StateTokenManager;
import com.ettrema.web.search.BaseResourceIndexer;
import com.ettrema.console.Result;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RequestContext;
import com.ettrema.context.RootContextLocator;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.User;
import com.ettrema.web.search.SearchManager;
import java.util.List;
import java.util.UUID;

public class Crawl extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Crawl.class);
    private static final long serialVersionUID = 1L;
    private final AsynchProcessor asynchProcessor;
    private final StateTokenManager stateTokenManager;
    private final RootContextLocator rootContextLocator;

    Crawl(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, AsynchProcessor asynchProcessor, StateTokenManager stateTokenManager, RootContextLocator rootContextLocator) {
        super(args, host, currentDir, resourceFactory);
        this.asynchProcessor = asynchProcessor;
        this.stateTokenManager = stateTokenManager;
        this.rootContextLocator = rootContextLocator;
    }

    @Override
    public Result execute() {
        log.debug("enqueueing host crawl");
        if (args.contains("cancel")) {
            if (CrawlFactory.thCrawl != null) {
                CrawlFactory.thCrawl.interrupt();
                return result("Cancelled crawl job");
            } else {
                return result("No crawl job is running");
            }
        } else {
            Resource cur = currentResource();
            if (!(cur instanceof CommonTemplated)) {
                return result("Resource is not compatible: " + cur.getClass());
            }
            CommonTemplated ct = (CommonTemplated) cur;
            Host h = ct.getHost();
            HostCrawler crawler = new HostCrawler(h.getNameNodeId());

            CrawlFactory.thCrawl = new Thread(crawler);
            CrawlFactory.thCrawl.start();

            log.debug("done enqueueing host crawl");

            return result("submitted crawl job");
        }
    }

    public class HostCrawler implements Runnable {

        final UUID hostNodeId;

        public HostCrawler(UUID hostNodeId) {
            this.hostNodeId = hostNodeId;
        }

        @Override
        public void run() {
            try {
                rootContextLocator.getRootContext().execute(new Executable2() {

                    @Override
                    public void execute(Context cntxt) {
                        try {
                            doProcess(cntxt);
                        } catch (InterruptedException ex) {
                            log.warn("Interupted crawl job now exiting");
                        }
                    }
                });
            } finally {
                CrawlFactory.thCrawl = null;
            }
        }

        public void doProcess(Context context) throws InterruptedException {
            VfsSession session = context.get(VfsSession.class);
            NameNode nHost = session.get(hostNodeId);
            if (nHost == null) {
                log.error("Name node for host does not exist: " + hostNodeId);
                return;
            }
            Object data = nHost.getData();
            if (data == null) {
                log.error("Data node does not exist. Name node: " + hostNodeId);
                return;
            }
            if (!(data instanceof Host)) {
                log.error("Node does not reference a Host. Instead references a: " + data.getClass() + " ID:" + hostNodeId);
                return;
            }

            Host h = (Host) data;

            SearchManager sm = RequestContext.getCurrent().get(SearchManager.class);
            boolean doSearchIndex = (sm != null);

            crawl(h, doSearchIndex);
            session.commit();
        }

        private void crawl(BaseResource res, boolean doSearchIndex) throws InterruptedException {
            log.debug("crawl: " + res.getHref());
            if (Thread.interrupted()) {
                log.info("Crawl job has been interrupted");
                throw new InterruptedException();
            }
            if (doSearchIndex) {
                BaseResourceIndexer indexer = new BaseResourceIndexer(res.getNameNodeId());
                asynchProcessor.enqueue(indexer);
            }
            if (res instanceof Folder) {
                Folder f = (Folder) res;
                for (Resource r : f.getChildren()) {
                    if (r instanceof BaseResource) {
                        crawl((BaseResource) r, doSearchIndex);
                    }
                }
            }
            if (res instanceof User) {
                User u = (User) res;
                log.info("Calculate CRC for user: " + u.getHref());
                stateTokenManager.calcBinaryCrc(u);
            }
        }

        @Override
        public String toString() {
            return "Crawler: " + hostNodeId;
        }
    }
}
