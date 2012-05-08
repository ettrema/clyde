package com.ettrema.web.console2;

import com.ettrema.web.BaseResource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.console.Result;
import com.ettrema.web.search.SearchManager;
import com.ettrema.web.search.SearchUtils;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

public class Search extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Search.class);
    private static final long serialVersionUID = 1L;
    private final SearchManager searchManager;

    Search(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, SearchManager searchManager) {
        super(args, host, currentDir, resourceFactory);
        this.searchManager = searchManager;
    }

    @Override
    public Result execute() {
        try {
            String query = "";
            for(String s : args ) {
                query += s + " ";
            }
            log.info("search: " + query);
            String hostName = currentResource().getHost().getName();
            Document[] docs = searchManager.search(hostName, query);
            log.info("found docs: " + docs.length);
            StringBuilder sb = new StringBuilder();
            sb.append("<ul>");
            for( Document d : docs ) {
                BaseResource res = SearchUtils.getResource(d);
                if( res != null ) {
                    sb.append("<li><a href='").append(res.getUrl()).append("'>").append(res.getUrl()).append("</a></li>");
                }
            }
            sb.append("</ul>");
            return result(sb.toString());
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        } catch (ParseException ex) {
            return result("exception: ", ex);
        } catch (CorruptIndexException ex) {
            return result("corrupt index: ", ex);
        }
    }
}
