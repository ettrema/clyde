
package com.bradmcevoy.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.Relationship;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.ettrema.console.Result;
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
            StringBuffer sbFrom = listRelations("from",from.getNameNode().findFromRelations(null));
            StringBuffer sbTo = listRelations("to",from.getNameNode().findToRelations(null));
            return result(sbFrom.toString() + sbTo.toString());
        }
    }

    private StringBuffer listRelations(String title, List<Relationship> rels) {
        StringBuffer sb = new StringBuffer("<h2>" + title + "</h2>");
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
