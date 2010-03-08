
package com.bradmcevoy.web.console2;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.search.HostSearchManager;
import com.ettrema.console.Result;
import java.util.List;
import java.util.UUID;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;

public class Search  extends AbstractConsoleCommand {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Search.class);
    
    Search(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }
    
    @Override
    public Result execute() {
        if (args.size() == 0) {
            return result("missing expression");
        }
        String exp = "";
        for( String s : args ) {
            exp += s + " ";
        }
        
        HostSearchManager mgr = HostSearchManager.getInstance(this.host);
        Hits hits;
        
        VfsSession session = RequestContext.getCurrent().get(VfsSession.class);
        try {
            hits = mgr.search(exp);
        } catch (ParseException ex) {
            return result("Couldnt parse search expression: " + ex.getMessage());
        }
        log.debug("Found " + hits.length() + " hits.");
        StringBuffer sb = new StringBuffer("Found " + hits.length() + " hits.<br/>");
        for(int i=0;i<hits.length();++i) {
            Document doc = HostSearchManager.doc(hits, i);
            List fields = doc.getFields();
            String sId = doc.get("id");
            if( sId == null || sId.length() == 0 ) {
                log.warn("No id for hit");
                sb.append(doc.get("name")).append("<br/>");
            } else {
                UUID id = UUID.fromString(sId);
                NameNode nn = session.get(id);
                BaseResource res = (BaseResource) nn.getData(); // todo: check type
                sb.append(res.getLink()).append("<br/>");
            }
        }
        return result(sb.toString());
    }

}
