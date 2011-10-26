package com.ettrema.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.User;
import com.ettrema.console.Result;
import java.util.List;

/**
 *
 * @author brad
 */
public class Group extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Group.class );

    public Group( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        if( args.isEmpty() ) {
            return result( "please enter a command" );
        }
        String cmd = args.get( 0 );
        Resource cur = this.currentResource();
        if( cur instanceof com.ettrema.web.Group ) {
            com.ettrema.web.Group g = (com.ettrema.web.Group) cur;
            if( cmd.equals( "add" ) || cmd.equals( "remove" ) ) {
                if( args.size() == 1 ) {
                    return result( "please enter a user to add to the current group" );
                }
                String user = args.get( 1 );
                Resource rUser = g.find( user );
                if( rUser == null ) {
                    return result( "user not found: " + user );
                } else if( rUser instanceof User ) {
                    User iuser = (User) rUser;
                    if( cmd.equals( "add" ) ) {
                        iuser.addToGroup( g );
                        commit();
                        return okResult( "added to group", g );
                    } else if( cmd.equals( "remove" ) ) {
                        iuser.removeFromGroup( g );
                        commit();
                        return okResult( "removed from group", g );
                    } else {
                        return result( "Unknown command: " + cmd + " Please use add or remove" );
                    }
                } else {
                    return result( "user path does not resolve to a User object. Is a: " + rUser.getClass().getCanonicalName() );
                }
            } else if( cmd.equals( "list" ) ) {
                return okResult( "listing current members:", g );
            } else {
                return result("unknown command: " + cmd);
            }
        } else {
            return result( "please execute this command from inside a group folder. Is a: " + cur.getClass() );
        }
    }

    private Result okResult( String desc, com.ettrema.web.Group g ) {
        String s = desc + "<br/>";
        s += "<ul>";
        for( User u : g.getMembers() ) {
            s += "<li>" + u.getLink() + "</li>";
        }
        s += "</ul>";
        return result( s );
    }
}
