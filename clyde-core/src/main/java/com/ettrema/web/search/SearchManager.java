package com.ettrema.web.search;

import com.ettrema.web.BaseResource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 *
 */
public class SearchManager {

    private static final Map<String,HostSearchManager> map = new ConcurrentHashMap<String, HostSearchManager>();

    private final DirectoryFactory directoryFactory;

    public SearchManager(DirectoryFactory directoryFactory) {
        this.directoryFactory = directoryFactory;
    }

    private synchronized HostSearchManager getInstance(String hostName) throws CorruptIndexException {
        HostSearchManager mgr = map.get(hostName);
        if( mgr == null ) {
            mgr = new HostSearchManager(directoryFactory, hostName);
            map.put(hostName, mgr);
        }
        return mgr;
    }

    public void index(BaseResource res) throws CorruptIndexException {
        String host = res.getHost().getName();
        HostSearchManager hsm = getInstance(host);
        hsm.index(res);
        synchronized(this) {
            map.remove(host);
        }
    }

    public Document[] search(String host, String query) throws ParseException, CorruptIndexException {
        String[] fields = new String[]{"title","name","html"};
        Map boosts = new HashMap();
        boosts.put("name", new Float(10));
        boosts.put("title", new Float(5));
        return search(host, fields,query,boosts);
    }

    public Document[] search(String host, String field, String query) throws ParseException, CorruptIndexException {
        String[] fields = new String[]{field};
        return search(host, fields,query, null);
    }

    public Document[] search(String host, String[] fields, String query, Map boosts) throws ParseException, CorruptIndexException {
        StringBuffer sb = new StringBuffer();
        // add a search term which is the quoted query and give it a x4 boost
        //sb.append('"').append(query).append('"').append("^4 ").append(query);
        sb.append(query);
        StandardAnalyzer ana = new StandardAnalyzer(Version.LUCENE_30);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, fields, ana); //, boosts);
        Query q = parser.parse(sb.toString());
        //Query q = new QueryParser(field, new StandardAnalyzer()).parse(sb.toString());
        BooleanQuery bq;

        HostSearchManager hsm = getInstance(host);
        return hsm.search(q);
    }

}
