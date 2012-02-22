package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.binary.StateTokenManager;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.context.RootContextLocator;
import com.ettrema.grid.AsynchProcessor;
import java.util.List;

public class CrawlFactory extends AbstractFactory {

    private final AsynchProcessor asynchProcessor;
    private final StateTokenManager stateTokenManager;
    private final RootContextLocator rootContextLocator;
    static Thread thCrawl;

    public CrawlFactory(AsynchProcessor asynchProcessor, StateTokenManager stateTokenManager, RootContextLocator rootContextLocator) {
        super("Crawl over all files and folders search indexing each item", new String[]{"crawl"});
        this.asynchProcessor = asynchProcessor;
        this.stateTokenManager = stateTokenManager;
        this.rootContextLocator = rootContextLocator;
    }

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new Crawl(args, host, currentDir, resourceFactory, asynchProcessor, stateTokenManager, rootContextLocator);
    }
}
