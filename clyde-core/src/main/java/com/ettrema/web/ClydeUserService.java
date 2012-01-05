package com.ettrema.web;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.context.Context;
import com.ettrema.context.Executable;
import com.ettrema.context.RootContext;
import com.ettrema.ftp.MiltonUser;
import com.ettrema.ftp.UserService;

/**
 *
 * @author brad
 */
public class ClydeUserService implements UserService{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydeUserService.class );
    private final ResourceFactory resourceFactory;
    private final RootContext rootContext;
    private final HostFinder hostFinder = new HostFinder();

    public ClydeUserService( ResourceFactory resourceFactory, RootContext rootContext ) {
        this.resourceFactory = resourceFactory;
        this.rootContext = rootContext;
    }
   

    @Override
    public MiltonUser getUserByName( final String name, final String hostName ) {
        log.debug( "getUserByName: " + name );
        return rootContext.execute( new Executable<MiltonUser>() {

            @Override
            public MiltonUser execute( Context context ) {
                NameAndAuthority na = NameAndAuthority.parse( name );
                Host host = findHost( hostName );
                if( host == null ) {
                    log.warn( "host not found: " + hostName);
                    return null;
                }
                host = host.findHost( na.authority);
                if( host == null ) {
                    log.warn("authority not found: " + na.authority);
                    return null;
                }
                User u = host.findUser( name );
                if( u != null ) {
                    log.debug( "found user: " + u.getName() );
                    MiltonUser mu = new MiltonUser( u, name + "@" + hostName, hostName );
                    return mu;
                } else {
                    return null;
                }
            }
        });

    }

    public Host findHost( String hostName ) {
        return hostFinder.getHost( hostName );
    }


    @Override
    public void save( MiltonUser arg0 ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public void delete( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean doesExist( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String[] getAllUserNames() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
