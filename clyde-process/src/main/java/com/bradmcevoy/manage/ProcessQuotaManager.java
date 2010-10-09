package com.bradmcevoy.manage;

import com.ettrema.event.DeleteEvent;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.ettrema.event.PutEvent;
import com.ettrema.event.ResourceEvent;
import com.bradmcevoy.process.ProcessDef;
import com.bradmcevoy.process.TokenValue;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.component.ComponentValue;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import com.ettrema.context.RootContext;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.Date;
import java.util.List;

/**
 *
 * @author brad
 */
public class ProcessQuotaManager extends VfsCommon implements EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ProcessQuotaManager.class );
    private final RootContext rootContext;
    private final String processName;
    private final QuotaManager quotaManager;

    public ProcessQuotaManager( RootContext rootContext, String processName, QuotaManager quotaManager) {
        this.rootContext = rootContext;
        this.processName = processName;
        this.quotaManager = quotaManager;

        this.rootContext.put( this );

        EventManager eventManager = rootContext.get( EventManager.class );
        if( eventManager == null ) {
            throw new RuntimeException( "Not available in config: " + EventManager.class );
        }
        eventManager.registerEventListener( this, PutEvent.class );
        eventManager.registerEventListener( this, DeleteEvent.class );

    }

    public void onEvent( Event e ) {
        log.debug( "onEvent: " + e.getClass() );
        long amount;
        Host host;
        if( !(e instanceof ResourceEvent)){
            return ;
        }
        ResourceEvent resourceEvent = (ResourceEvent) e;
        if( !(resourceEvent.getResource() instanceof BaseResource) ) {
            log.trace( "not  a baseresource");
            return ;
        }
        if( e instanceof PutEvent ) {
            PutEvent event = (PutEvent) e;
            BaseResource r = (BaseResource) event.getResource();
            if( r.getContentLength() != null ) {
                amount = r.getContentLength();
                host = r.getHost();
            } else {
                log.debug( "no content length associated with: " + r.getHref());
                return ;
            }
        } else if( e instanceof DeleteEvent ) {
            DeleteEvent event = (DeleteEvent) e;
            BaseResource r = (BaseResource) event.getResource();
            if( r != null ) {
                if( r instanceof Folder ) {
                    log.debug( "ignoring folder delete");
                    return ;
                } else {
                    if(r.getContentLength() != null ) {
                        amount = r.getContentLength() * -1;
                        host = r.getHost();
                    } else {
                        log.warn( "no contentlength associated with resource: " + r.getHref() + ". Usage will not be accurate");
                        return ;
                    }
                }
            } else {
                log.warn("null resource associated with delete event");
                return ;
            }
        } else {
            // ignore event
            return ;
        }
        quotaManager.incrementUsage( host, amount );
        scheduleCheck( host );
    }

    private void scheduleCheck( Host host ) {

        AsynchProcessor proc = RequestContext.getCurrent().get( AsynchProcessor.class );
        if( proc == null ) {
            log.warn( "no " + AsynchProcessor.class );
            return;
        } else {
            proc.enqueue( new CheckProcessTask( host.getName(), new Date() ) );
        }
    }

    void checkProcess( String hostName, Context context, Date scheduledAt ) {
        log.debug( "checkProcess: " + hostName );
        VfsSession sess = context.get( VfsSession.class );
        if( sess == null )
            throw new RuntimeException( "Couldnt get from context: " + VfsSession.class );
        List<NameNode> list = sess.find( Host.class, hostName );
        if( list == null || list.size() == 0 ) {
            log.warn( "Couldnt find host: " + hostName );
        } else {
            Host host = (Host) list.get( 0 ).getData();
            if( host == null ) {
                log.warn( "Found host node, but no associated host data item: " + list.get( 0 ).getId() );
                return;
            }
            ComponentValue cv = host.getValues().get( processName );
            if( cv == null ) {
                log.warn( "no value called: " + processName );
                return;
            }
            Object oProcess = cv.getValue();
            if( oProcess == null ) {
                log.warn( "Did not find a process token at value name: " + processName );
                return;
            }
            if( !( oProcess instanceof TokenValue ) ) {
                log.warn( "Process name did not locate a token. Got a: " + oProcess.getClass() );
                return;
            }
            // always save and commit, becaus even if we didnt transition
            // we might have updated cached usage data
            ProcessDef.scan( cv, host );
            host.save();
            commit();
        }
    }
}
