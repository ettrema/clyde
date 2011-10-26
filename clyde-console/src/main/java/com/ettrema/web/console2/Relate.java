
package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.Relationship;
import java.util.List;

public class Relate extends AbstractConsoleCommand{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Relate.class);
    
    public static final int MAX_LENGTH = 100;
    
    Relate(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    public Result execute() {
        Folder from = this.currentResource();
        if( args.size() > 0 ) {
            String relationName = args.get(0);
            Path toPath = Path.path(args.get(1));
            Folder to = (Folder) this.find(toPath);
            if( to == null ) {
                return result("cant find: " + toPath);
            } else {
                Relationship r = from.getNameNode().makeRelation(to.getNameNode(), relationName);
                to.getNameNode().onNewRelationship(r);
                commit();
                return result("created relationship");
            }
        } else {
            StringBuffer sbFrom = listFromRelations(from);
            StringBuffer sbTo = listToRelations(from);
            return result(sbFrom.toString() + sbTo.toString());
        }
    }

    private StringBuffer listToRelations(Folder from) {
        List<Relationship> rels = from.getNameNode().findToRelations(null);
        StringBuffer sb = new StringBuffer("<h2>to</h2>");
        sb.append("<ul>");
        for (Relationship r : rels) {
            DataNode dn = r.from().getData();
            if (dn instanceof BaseResource) {
                BaseResource res = (BaseResource) dn;
                sb.append("<li>").append(res.getHref()).append('(').append(r.relationship()).append(')').append("</li>");
            } else {
                sb.append("<li>").append(r.to().getName()).append('(').append(r.relationship()).append(')').append("</li>");
            }
        }
        sb.append("</ul>");
        return sb;
    }

    private StringBuffer listFromRelations(Folder from) {
        List<Relationship> rels = from.getNameNode().findFromRelations(null);
        StringBuffer sb = new StringBuffer("<h2>from</h2>");
        sb.append("<ul>");
        for (Relationship r : rels) {
            DataNode dn = r.to().getData();
            if (dn instanceof BaseResource) {
                BaseResource res = (BaseResource) dn;
                sb.append("<li>").append(res.getHref()).append('(').append(r.relationship()).append(')').append("</li>");
            } else {
                sb.append("<li>").append(r.to().getName()).append('(').append(r.relationship()).append(')').append("</li>");
            }
        }
        sb.append("</ul>");
        return sb;
    }

}
