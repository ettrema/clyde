
package com.ettrema.process;

import com.ettrema.vfs.VfsCommon;
import com.ettrema.web.BaseResource;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.UUID;

/**
 * The parent of a TimerProcessor must be a Token. This processor will scan the
 * token 
 * 
 * @author brad
 */
public class TimerProcessor extends VfsCommon implements DataNode, Serializable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TimerProcessor.class);
    
    private static final long serialVersionUID = 1L;

    private UUID id;
    
    private String processName;
        
    private transient NameNode nameNode;

    public static void create(BaseResource parentRes, String processName) {
        ComponentValue cv = parentRes.getValues().get(processName);
        TokenValue token = (TokenValue) cv.getValue();        
        TimerProcessor proc = new TimerProcessor(token.getProcessName());
        String name = "timer_" + token.getProcessName();
        if( parentRes.hasChildNode(name) ) {
            log.trace("timer node already exists");
            return ;
        }
        NameNode newNode = parentRes.addChildNode(name, proc);
        newNode.save();
        log.debug("..created timer in: " + parentRes.getHref() );
    }    
    
    public TimerProcessor(String tokenName) {
        this.processName = tokenName;
    }    

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public NameNode getNameNode() {
        return nameNode;
    }

    public void init(NameNode nameNode) {
        this.nameNode = nameNode;
    }

    public void onDeleted(NameNode nameNode) {
    }

    public void pleaseImplementSerializable() {
    }

    public String getProcessName() {
        return processName;
    }


}
