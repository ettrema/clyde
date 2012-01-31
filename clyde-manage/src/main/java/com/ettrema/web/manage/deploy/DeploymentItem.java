package com.ettrema.web.manage.deploy;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class DeploymentItem implements Serializable, Comparable<DeploymentItem> {
    private static final long serialVersionUID = 1L;
    
    private UUID itemId;
    private String path;
    private Long size;
    private String clazz;
    private boolean directory;
    private boolean created;
    

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

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public int compareTo(DeploymentItem o) {
        return this.path.compareTo(o.path);
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    
    
    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
        
    
}
