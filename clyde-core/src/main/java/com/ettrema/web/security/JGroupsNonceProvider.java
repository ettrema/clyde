package com.ettrema.web.security;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.Nonce;
import com.bradmcevoy.http.http11.auth.NonceProvider;
import com.bradmcevoy.http.http11.auth.SimpleMemoryNonceProvider;
import com.ettrema.channel.Channel;
import com.ettrema.channel.ChannelListener;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class JGroupsNonceProvider implements NonceProvider, ChannelListener {

    private static final Logger log = LoggerFactory.getLogger( JGroupsNonceProvider.class );
    private final SimpleMemoryNonceProvider wrapped;
    private final Channel channel;
    private final Map<UUID, Nonce> nonces;

    public JGroupsNonceProvider( Channel channel, int nonceValiditySeconds ) {
        this.nonces = new ConcurrentHashMap<UUID, Nonce>();
        this.wrapped = new SimpleMemoryNonceProvider( nonceValiditySeconds, nonces );
        this.channel = channel;
        channel.registerListener( this );
    }

    @Override
    public NonceValidity getNonceValidity( String nonce, Long nonceCount ) {
        return wrapped.getNonceValidity( nonce, nonceCount );
    }

    @Override
    public String createNonce( Resource resource, Request request ) {
        log.debug( "createNonce" );
        Nonce nonce = wrapped.createNonceObject( resource, request );
        notifyNewNonce( nonce );
        return nonce.getValue().toString();
    }

    private void notifyNewNonce( Nonce nonce ) {
        channel.sendNotification( new NewNonce( nonce ) );
    }

    @Override
    public void handleNotification( UUID sourceId, Serializable msg ) {
        if( msg instanceof NewNonce ) {
            NewNonce newNonce = (NewNonce) msg;
            log.debug( "got new nonce: " + newNonce.nonce.getValue() );
            nonces.put( newNonce.nonce.getValue(), newNonce.nonce );
        }
    }

    public void memberRemoved( UUID sourceId ) {

    }

    public void onConnect() {

    }

    public static class NewNonce implements Serializable {

        private static final long serialVersionUID = 1L;
        public final Nonce nonce;

        public NewNonce( Nonce nonce ) {
            if( nonce == null) throw new IllegalArgumentException( "nonce cannot be null");
            this.nonce = nonce;
        }
    }
}
