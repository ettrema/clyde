package com.bradmcevoy.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.groups.GroupService;
import com.bradmcevoy.web.security.Permission;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import com.bradmcevoy.web.security.Subject;
import com.bradmcevoy.web.security.UserGroup;
import com.ettrema.console.Result;
import com.ettrema.mail.MailboxAddress;
import java.util.ArrayList;
import java.util.List;

import static com.ettrema.context.RequestContext._;

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
        if( args.size() != 3) {
            return result("need 3 args");
        }
        String roleName = args.get( 0 );
        String sUser = args.get( 1 );
        String userName;
        String userHostName;
        if( sUser.contains( "@")) {
            MailboxAddress mbox = MailboxAddress.parse( sUser );
            userName = mbox.user;
            userHostName = mbox.domain;
        } else {
            userName = sUser;
            userHostName = host;
        }
        String srcPath = args.get( 2 );

        log.debug( "grant: " + roleName + " - " + sUser + "->" + srcPath );
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
            List<Subject> users = new ArrayList<Subject>();
            if( userName.equals( "*" ) ) {
                users = findUsers( target, userHostName );
            } else {
                User user = findUser( target, userHostName, userName );
                if( user == null ) {
                    log.trace( "look for group: " + sUser);
                    UserGroup userGroup = _(GroupService.class).getGroup( target, sUser );
                    if( userGroup == null ) {
                        return result( "user not found" );
                    }
                    users.add( userGroup );
                }
            }

            String res = "Granted ok<br/>";
            for( Subject user : users ) {
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
                
                for( Permission perm : target.permissions() ) {
                    res += perm.toString() + "<br/>";
                }
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

    private List<Subject> findUsers( BaseResource target, String userHostName ) {
        Host h = target.getHost();
        while( h != null ) {
            if( h.getName().equals( userHostName ) ) {
                List<Subject> users = new ArrayList<Subject>();
                for( Resource r : h.getUsers().getChildren() ) {
                    if( r instanceof User ) {
                        users.add( (User) r );
                    }
                }
                return users;
            }
            h = h.getParentHost();
        }
        return null;

    }
}
