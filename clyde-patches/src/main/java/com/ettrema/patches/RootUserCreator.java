package com.ettrema.patches;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable2;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.Organisation;
import com.bradmcevoy.web.User;
import com.ettrema.common.Service;
import java.util.List;

/**
 *
 * @author brad
 */
public class RootUserCreator implements Service {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RootUserCreator.class);
    private final RootContext rootContext;
    private String hostName;
    private String userName;
    private String password;

    public RootUserCreator( RootContext rootContext, String hostName, String userName, String password ) {
        this.rootContext = rootContext;
        this.hostName = hostName;
        this.userName = userName;
        this.password = password;
    }

    private void checkAndCreate( Context context ) {
        VfsSession sess = context.get(VfsSession.class);
        List<NameNode> list = sess.find( Organisation.class, hostName );
        if( list == null || list.size()==0 ) {
            log.debug( "no organisation found: " + hostName);
            return ;
        }
        for( NameNode nn : list) {
            Organisation org = (Organisation) nn.getData();
            if( org == null ) {
                log.warn( "node contains null data object: " + nn.getId());
            } else {
                Resource r = org.getUsers( true ).child( userName );
                if( r == null ) {
                    log.debug( "creating user: " + userName + " in organisation: " + org.getPath());
                    User u = org.createUser( userName, password );
                    u.save();
                } else {
                    log.debug( "found an existing resource: " + r.getClass());
                }
            }
        }
        sess.commit();

    }


    private void checkAndCreate() {
        rootContext.execute( new Executable2() {

            public void execute( Context context ) {
                checkAndCreate( context );
            }
        } );

    }

    public void start() {
        checkAndCreate();
    }

    public void stop() {
        
    }
}
