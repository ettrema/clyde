
package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.console.Result;
import java.util.List;

public class Alias extends MkHost {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Alias.class);
    
    Alias(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    protected Result validate(Folder cur, String newName) {
        Result r = super.validate(cur, newName);
        if( r != null ) return r;
        String sAliasPath = args.get(1);
        if( sAliasPath == null || sAliasPath.length() == 0 ) {
            return result("Please supply an alias");
        } else {
            return null;
        }
    }

    
    
    @Override
    protected Host doCreate(Folder parent, String newName) {
        Host newHost = super.doCreate(parent, newName);
        String sAliasPath = args.get(1);
        Path pAliasPath = Path.path(sAliasPath);
        newHost.setAliasPath(pAliasPath); 
        newHost.save();
        return newHost;
    }


}
