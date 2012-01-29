package com.ettrema.web.manage.deploy;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class DeploymentItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private UUID itemId;

    public DeploymentItem() {
    }

    public DeploymentItem(UUID itemId) {
        this.itemId = itemId;
    }
       

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}
