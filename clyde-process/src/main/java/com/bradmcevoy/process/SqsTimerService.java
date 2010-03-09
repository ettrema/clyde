package com.bradmcevoy.process;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.grid.Processable;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class SqsTimerService extends VfsCommon implements TimerService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydeTimerService.class );

    public SqsTimerService( RootContext context ) {
        log.debug( "Hello, from the SqsTimerService" );
    }

    public void registerTimer( TimerRule timerRule, ProcessContext context ) {
        log.debug( "registerTimer: " + context.getCurrentState().getProcess().getName() );
        TokenValue token = (TokenValue) context.token;
        BaseResource page = (BaseResource) token.getContainer();
        TimerProcessor proc = TimerProcessor.create( page, context.token.getProcessName() );
        log.debug( "..created " + proc.getClass().getName() );
    }

    public void unRegisterTimer( TimerRule timerRule, ProcessContext context ) {
        log.debug( "unRegisterTimer" );
        TokenValue token = (TokenValue) context.token;
        BaseResource page = (BaseResource) token.getContainer();
        page.removeChildNode( "timer_" + token.getProcessName() );
    }

    public synchronized void doProcess( Context context ) {
//        if( timers == null || pos >= timers.size() ) {
//            timers = vfs().find( TimerProcessor.class, null );
//            pos = 0;
//        }
//        if( timers.isEmpty() ) {
//            timers = null;
//            return;
//        }
////        log.debug("scheduling timer: " + pos);
//        NameNode timerToProcess = timers.get( pos );
//        TimerProcessor proc = (TimerProcessor) timerToProcess.getData();
//        asynchProc.enqueue( proc );
//        pos++;
    }

    public static SqsTimerProcessor create( BaseResource parentRes, String processName ) {
        ComponentValue cv = parentRes.getValues().get( processName );
        TokenValue token = (TokenValue) cv.getValue();
        SqsTimerProcessor proc = new SqsTimerProcessor( token.getProcessName() );
        return proc;
    }

    public static class SqsTimerProcessor extends VfsCommon implements Serializable, Processable {

        private static final long serialVersionUID = 1L;
        UUID id;
        String processName;
        transient NameNode nameNode;

        public SqsTimerProcessor( String tokenName ) {
            this.processName = tokenName;
        }

        /**
         * Locate the process and the token, build a processcontext and perform a scan
         *
         */
        public void doProcess( Context context ) {
//        log.debug("doProcess: " + this.getPath());
            try {
                NameNode parentNode = this.nameNode.getParent();
                DataNode parentDataNode = parentNode.getData();
                BaseResource parentRes = (BaseResource) parentDataNode;

                ComponentValue cv = parentRes.getValues().get( processName );
                Object o = cv.getValue();
                if( o == null ) {
                    throw new NullPointerException( "The timer value which should contain a TokenValue object is null" );
                }
                if( !( o instanceof TokenValue ) ) {
                    throw new RuntimeException( "Found a timer but its not a valid type. Require a TokenValue but is a: " + o.getClass() );
                }
                TokenValue token = (TokenValue) cv.getValue();

                ComponentDef cdef = cv.getDef( parentRes );
                ProcessDef pdef = (ProcessDef) cdef;

                ProcessContext processContext = new ProcessContext( token, pdef );
                boolean didTransition = processContext.scan();

                if( didTransition ) {
                    log.debug( "timer did transition. saving: " + parentRes.getHref() );
                    parentRes.save();
                    commit();
                } else {
//                log.debug("timer did not transition");
                    rollback();
                }
            } catch( Throwable e ) {
                rollback();
                log.error( "Exception processing timer node: datanodeid: " + this.id + " namenodeid: " + nameNode.getId(), e );
            }
        }

        public void setId( UUID id ) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }

        public void init( NameNode nameNode ) {
            this.nameNode = nameNode;
        }

        public void onDeleted( NameNode nameNode ) {
        }

        public void pleaseImplementSerializable() {
        }
    }
}

