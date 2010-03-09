
package com.bradmcevoy.process;

import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BaseResource;

/**
 * Implements creating and removing persistent timer objects. Once persisted, these
 * will be detected and processed by the ScheduledTimerRunner
 *
 * DEPRECATED for production use. use SqsTimerService instead
 *
 * Does not implement serializable as it won't work in a distributed environment
 *
 * @author brad
 */
public class ClydeTimerService extends VfsCommon implements TimerService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeTimerService.class);

        
    public ClydeTimerService() {
        log.debug("Hello, from the timer service");
    }
        
    public void registerTimer(TimerRule timerRule, ProcessContext context) {
        log.debug("registerTimer: " + context.getCurrentState().getProcess().getName());
        TokenValue token = (TokenValue) context.token;
        BaseResource page = (BaseResource) token.getContainer();
        TimerProcessor proc = TimerProcessor.create(page, context.token.getProcessName());
        log.debug("..created " + proc.getClass().getName());
    }

    public void unRegisterTimer(TimerRule timerRule, ProcessContext context) {
        log.debug("unRegisterTimer");
        TokenValue token = (TokenValue) context.token;
        BaseResource page = (BaseResource) token.getContainer();
        page.removeChildNode("timer_" + token.getProcessName());        
    }
    
}
