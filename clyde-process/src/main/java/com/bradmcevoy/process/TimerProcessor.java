
package com.bradmcevoy.process;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.grid.Processable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.Serializable;
import java.util.UUID;

/**
 * The parent of a TimerProcessor must be a Token. This processor will scan the
 * token 
 * 
 * @author brad
 */
public class TimerProcessor extends VfsCommon implements DataNode, Serializable, Processable{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimerProcessor.class);
    
    private static final long serialVersionUID = 1L;

    UUID id;
    
    String processName;
        
    transient NameNode nameNode;

    public static TimerProcessor create(BaseResource parentRes, String processName) {
        ComponentValue cv = parentRes.getValues().get(processName);
        TokenValue token = (TokenValue) cv.getValue();        
        TimerProcessor proc = new TimerProcessor(token.getProcessName());
        NameNode newNode = parentRes.addChildNode("timer_" + token.getProcessName(), proc);
        newNode.save();
        return proc;
    }    
    
    public TimerProcessor(String tokenName) {
        this.processName = tokenName;
    }    

    /**
     * Locate the process and the token, build a processcontext and perform a scan
     * 
     */    
    public void doProcess(Context context) {
//        log.debug("doProcess: " + this.getPath());
        try {
            NameNode parentNode = this.nameNode.getParent();
            DataNode parentDataNode = parentNode.getData();
            BaseResource parentRes = (BaseResource) parentDataNode;
            
            ComponentValue cv = parentRes.getValues().get(processName);
            Object o = cv.getValue();
            if( o == null ) {
                throw new NullPointerException("The timer value which should contain a TokenValue object is null");
            }
            if( !(o instanceof TokenValue) ) {
                throw new RuntimeException("Found a timer but its not a valid type. Require a TokenValue but is a: " + o.getClass());
            }
            TokenValue token = (TokenValue) cv.getValue();
            
            ComponentDef cdef = cv.getDef(parentRes);
            ProcessDef pdef = (ProcessDef) cdef;
            
            ProcessContext processContext = new ProcessContext(token, pdef);
            boolean didTransition = processContext.scan();

            if (didTransition) {
                log.debug("timer did transition. saving: " + parentRes.getHref());
                parentRes.save();
                commit();                
            } else {
//                log.debug("timer did not transition");
                rollback();
            }
        } catch (Throwable e) {            
            rollback();        
            log.error("Exception processing timer node: datanodeid: " + this.id + " namenodeid: " + nameNode.getId(), e);
        }
    }    
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void init(NameNode nameNode) {
        this.nameNode = nameNode;
    }

    public void onDeleted(NameNode nameNode) {
    }

    public void pleaseImplementSerializable() {
    }

}
