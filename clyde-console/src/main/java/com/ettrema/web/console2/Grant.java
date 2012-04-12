package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.RequestParams;
import com.ettrema.web.User;
import com.ettrema.web.groups.GroupService;
import com.ettrema.web.security.Permission;
import com.ettrema.web.security.PermissionChecker;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.Permissions;
import com.ettrema.web.security.Subject;
import com.ettrema.web.security.UserGroup;
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
        if( args.size() != 3 ) {
            return result( "need 3 args" );
        }
        String roleName = args.get( 0 );
        String sUser = args.get( 1 );
        String userName;
        String userHostName;
        if( sUser.contains( "@" ) ) {
            MailboxAddress mbox = MailboxAddress.parse( sUser );
            userName = mbox.user;
            userHostName = mbox.domain;
        } else {
            userName = sUser;
            userHostName = host;
        }
        String srcPath = args.get( 2 );

        log.debug( "grant: " + roleName + " - " + sUser + "->" + srcPath );
        Folder curFolder;
        try {
            curFolder = currentResource();
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        }

        Permissions perms;
        if( curFolder == null ) {
            log.debug( "current folder not found: " + currentDir );
            return result( "current dir not found: " + currentDir );
        } else {
            Path pSrc = Path.path( srcPath );
            BaseResource target = (BaseResource) curFolder.find( pSrc );
            if( target == null ) {
                return result( "Couldnt find: " + pSrc );
            }
            List<Subject> users = new ArrayList<>();
            if( userName.equals( "*" ) ) {
                users = findUsers( target, userHostName );
            } else {
                User user = findUser( target, userHostName, userName );
                if( user == null ) {
                    log.trace( "look for group: " + sUser );
                    UserGroup userGroup = _( GroupService.class ).getGroup( target, sUser );
                    if( userGroup == null ) {
                        return result( "user and user group not found" );
                    }
                    log.trace("found user group: " + userGroup.getSubjectName());
                    users.add( userGroup );
                } else {
                    log.trace("found user: " + user.getName());
                    users.add( user );
                }
            }

            String res = "Granted ok<br/>";
            PermissionChecker checker = ctx().get( PermissionChecker.class );
            log.trace("granting to users: " + users.size());
            for( Subject user : users ) {
                log.trace( "grant to: " + user );
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

                perms = target.permissions();
                if( perms.isEmpty() ) {
                    log.warn("grant appeared to succeed, but there are no permissions!!!");
                } else {
                    log.trace("perms: " + perms.size());
                    for( Permission perm : perms ) {
                        res += perm.toString() + "<br/>";
                    }
                }
            }
            commit();
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
