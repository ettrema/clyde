package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import static com.ettrema.context.RequestContext.*;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class Rename extends AbstractConsoleCommand{
    public Rename(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    public Result execute() {
        if( args.size() != 2 ) {
            return result( "need two arguments");
        }
        String sPath = args.get(0);
        String newName = args.get(1);

        NameNode node;
        try{
            UUID uuid = UUID.fromString(sPath);
            VfsSession sess = _(VfsSession.class);
            node = sess.get(uuid);
            if( node == null ) {
                return result("No such node: " + uuid);
            }
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
            } else if( list.size() > 1) {
                return result("multiple items found");
            }
            node = list.get(0).getNameNode();
        }
        node.setName( newName );
        node.save();
        commit();
        return result( "renamed to: " + newName);
    }
}
