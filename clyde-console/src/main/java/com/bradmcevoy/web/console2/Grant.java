package com.bradmcevoy.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.Permission;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.ettrema.console.Result;
import java.util.List;

/**
 *
 * @author brad
 */
public class Grant extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Grant.class );

    public Grant( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        String roleName = args.get( 0 );
        String userName = args.get( 1 );
        String userHostName = args.get( 2 );
        String srcPath = args.get( 3 );
        log.debug( "grant: " + roleName + " - " + userName + "->" + srcPath );
        Folder curFolder = currentResource();
        if( curFolder == null ) {
            log.debug( "current folder not found: " + currentDir );
            return result( "current dir not found: " + currentDir );
        } else {
            Path pSrc = Path.path( srcPath );
            BaseResource target = (BaseResource) curFolder.find( pSrc );
            if( target == null ) {
                return result( "Couldnt find: " + pSrc );
            }
            User user = findUser( target, userHostName, userName );
            if( user == null ) {
                return result( "user not found" );
            }


            PermissionChecker checker = ctx().get( PermissionChecker.class );

            // check current user has admin
            Auth auth = RequestParams.current().getAuth();
            if( !checker.hasRole( Role.ADMINISTRATOR, target, auth ) ) {
                return result( "you do not have admin priviledge on target" );
            }

            Role role = Role.valueOf( roleName );
            if( role.equals( Role.OWNER ) && !checker.hasRole( Role.SYSADMIN, target, auth ) ) {
                return result( "cant make owner" );
            }
            if( role.equals( Role.SYSADMIN ) ) {
                return result( "you're kidding, right??" );
            }
            target.permissions( true ).grant( role, user );
            commit();

            String res = "Granted ok<br/>";
            for( Permission perm : target.permissions() ) {
                res += perm.toString() + "<br/>";
            }
            return result( res );
        }
    }

    private User findUser( BaseResource target, String userHostName, String userName ) {
        Host h = target.getHost();
        while( h != null ) {
            if( h.getName().equals( userHostName ) ) {
                return h.findUser( userName );
            }
            h = h.getParentHost();
        }
        return null;
    }
}
