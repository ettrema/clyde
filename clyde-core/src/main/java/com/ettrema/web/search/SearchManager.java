package com.ettrema.web.search;

import com.ettrema.common.Service;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.BaseResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 *
 */
public class SearchManager implements Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchManager.class);
    private static final Map<String, HostSearchManager> map = new ConcurrentHashMap<>();
    private final DirectoryFactory directoryFactory;
    
    private boolean running;

    //private final SearcherManager mgr;
    public SearchManager(DirectoryFactory directoryFactory) {
        this.directoryFactory = directoryFactory;
    }

    private synchronized HostSearchManager getInstance(String hostName) throws CorruptIndexException {
        HostSearchManager mgr = map.get(hostName);
        if (mgr == null || mgr.isClosed()) {
            mgr = new HostSearchManager(directoryFactory, hostName);
            map.put(hostName, mgr);
        }
        return mgr;
    }

    public void index(BaseResource res) throws CorruptIndexException {
        if( !running ) {
            log.trace("Not indexing, because search manager is not started");
            return ;
        }
        String host = res.getHost().getName();
        HostSearchManager hsm = getInstance(host);
        hsm.index(res);
    }

    public Document[] search(String host, String query) throws ParseException, CorruptIndexException {
        String[] fields = new String[]{"title", "name", "body"};
        Map boosts = new HashMap();
        boosts.put("name", new Float(10));
        boosts.put("title", new Float(5));
        return search(host, fields, query, boosts);
    }

    public Document[] search(String host, String field, String query) throws ParseException, CorruptIndexException {
        String[] fields = new String[]{field};
        return search(host, fields, query, null);
    }

    public Document[] search(String host, String[] fields, String query, Map boosts) throws ParseException, CorruptIndexException {
        StringBuilder sb = new StringBuilder();
        // add a search term which is the quoted query and give it a x4 boost
        //sb.append('"').append(query).append('"').append("^4 ").append(query);
        LogUtils.trace(log, "search: query:", query, "host:", host);
        sb.append(query);
        HostSearchManager hsm = getInstance(host);
        StandardAnalyzer ana = hsm.getAnalyzer();
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, fields, ana); //, boosts);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        Query q = parser.parse(sb.toString());
        LogUtils.trace(log, "search: query", q);
        return hsm.search(q);
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        List<HostSearchManager> toClose = new ArrayList<>(map.values());
        for (HostSearchManager hsm : toClose) {
            hsm.close();
        }
    }
}
