package com.bradmcevoy.process;

import com.bradmcevoy.vfs.VfsCommon;
import com.ettrema.context.Context;
import com.ettrema.grid.AsynchProcessor;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author brad
 */
public class ClydeTimerJob extends VfsCommon implements Processable, Serializable{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeTimerJob.class);

    private static final long serialVersionUID = 1L;

    /**
     * Temporary cache for found timers that need executing
     */
    private List<NameNode> timers;
    private int pos;


    public synchronized void doProcess(Context context) {
        log.debug( "doProcess");
        if (timers == null || pos >= timers.size()) {
            timers = vfs().find(TimerProcessor.class, null);
            pos = 0;
        }
        if( timers.isEmpty() ) {
            timers = null;
            return ;
        }
        log.debug("scheduling timer: " + pos);
        NameNode timerToProcess = timers.get(pos);
        TimerProcessor proc = (TimerProcessor) timerToProcess.getData();
        AsynchProcessor asynchProc = requestContext().get( AsynchProcessor.class );
        asynchProc.enqueue(proc);
        pos++;
    }

    public void pleaseImplementSerializable() {

    }

}
