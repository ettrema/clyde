package com.bradmcevoy.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.ettrema.console.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class Rm extends AbstractConsoleCommand{
    public Rm(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    public Result execute() {
        String sPath = args.get(0);

        try{
            UUID uuid = UUID.fromString(sPath);
            VfsSession sess = RequestContext.getCurrent().get(VfsSession.class);
            NameNode node = sess.get(uuid);
            if( node == null ) {
                return result("No such node: " + uuid);
            }
            node.delete();
            sess.commit();
            return result("deleted: " + uuid);
        } catch(IllegalArgumentException e) {
            // ok, not a uuid
            Path path = Path.path(sPath);
            List<BaseResource> list = new ArrayList<BaseResource>();
            Folder curFolder = currentResource();
            Result resultSearch = findWithRegex(curFolder, path, list);
            if (resultSearch != null) {
                return resultSearch;
            }

            if( list.size() == 0 ) {
                return result("not found: " + sPath);
            }
            StringBuffer sb = new StringBuffer();
            for( BaseResource r : list ) {
                r._delete();
                sb.append(r.getHref()).append(",");
            }
            commit();
            return result("deleted " + sb.toString());
        }
    }
}
