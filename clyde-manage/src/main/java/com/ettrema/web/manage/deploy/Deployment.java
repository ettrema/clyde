
package com.ettrema.web.manage.deploy;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class Deployment implements DataNode, Serializable{
    private static final long serialVersionUID = 1L;

    private UUID id;
    
    private transient NameNode nameNode;
    
    private List<DeploymentItem> items;
    
    
    public String getName() {
        return nameNode.getName();
    }
    
    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void init(NameNode nameNode) {
        this.nameNode = nameNode;
    }

    @Override
    public void onDeleted(NameNode nameNode) {
        
    }

    public List<DeploymentItem> getItems() {
        return items;
    }

    public void setItems(List<DeploymentItem> items) {
        this.items = items;
    }

    public Date getCreatedDate() {
        return nameNode.getCreatedDate();
    }
    
    public Date getModifiedDate() {
        return nameNode.getModifiedDate();
    }

    public void delete() {
        nameNode.delete();
    }
    
}
