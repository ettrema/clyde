package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.User;
import com.ettrema.console.Result;
import java.util.List;

public class MkUser extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MkUser.class );
    public User newUsetr;

    MkUser( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    protected User doCreate( String newName, String password ) {
        Host host = currentResource().getHost();
        return host.createUser( newName, password );
    }

    @Override
    public Result execute() {
        if( args.size() != 2 ) {
            return result( "need two arguments" );
        }
        String newName = args.get( 0 );
        String password = args.get( 1 );
        log.debug( "mkdir. execute: " + newName );
        Folder cur = currentResource();
        if( cur == null ) {
            log.debug( "current resource not found: " + currentDir );
            return result( "current dir not found: " + currentDir );
        } else {
            Result validationResult = validate( cur, newName );
            if( validationResult != null ) {
                return validationResult;
            }
            newUsetr = doCreate( newName, password );
            commit();
            return new Result( currentDir, "created: " + newUsetr.getLink() );
        }
    }

    protected Result validate( Folder cur, String newName ) {
        Resource existing = cur.child( newName );
        if( existing != null ) {
            return result( "An item of that name already exists. Is a: " + existing.getClass() );
        } else {
            return null;
        }
    }
}