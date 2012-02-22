package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.web.search.SearchManager;
import java.util.List;

public class SearchFactory extends AbstractFactory {

    private final SearchManager searchManager;
    static Thread thCrawl;

    public SearchFactory(SearchManager searchManager) {
        super("Do a full text search on the current host", new String[]{"search"});
        this.searchManager = searchManager;
    }

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new Search(args, host, currentDir, resourceFactory, searchManager);
    }
}
