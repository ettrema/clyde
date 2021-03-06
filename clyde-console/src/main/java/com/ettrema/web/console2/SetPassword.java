package com.ettrema.web.console2;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.web.User;
import com.ettrema.web.security.PermissionChecker;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.console.Result;
import java.util.List;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class SetPassword extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Mv.class );

    public SetPassword( List<String> args, String host, String currentDir, ResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        try {
            String sUser = args.get( 0 );
            String newPassword = args.get( 1 );
            User user = currentResource().getHost().findUser( sUser );
            if( user == null ) {
                return result( "user not found" );
            }

            Request req = _( CurrentRequestService.class ).request();
            if( req == null ) throw new RuntimeException( "No current request" );
            Auth auth = req.getAuthorization();
            boolean isSourceAuthor = _( PermissionChecker.class ).hasRole( Role.AUTHOR, user, auth );
            if( !isSourceAuthor ) {
                return result( "You do not have the AUTHOR role on the source" );
            }
            user.setPassword( newPassword );
            user.save();
            commit();

            return result( "updated: " + user.getLink() );
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("can't lookup current resource", ex);
        }


    }
}
