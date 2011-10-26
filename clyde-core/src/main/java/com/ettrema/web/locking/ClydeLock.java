package com.bradmcevoy.web.locking;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class ClydeLock implements DataNode, Serializable{
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID lockedByUserId;
    private Date lockedUntil;
    private long duration;

    private transient NameNode node;

    public void setId( UUID id ) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void init( NameNode nameNode ) {
        this.node = nameNode;
    }

    public void onDeleted( NameNode nameNode ) {
        
    }

    public UUID getLockedByUserId() {
        return lockedByUserId;
    }

    public Date getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedByUserId( UUID lockedByUserId ) {
        this.lockedByUserId = lockedByUserId;
    }

    public void setLockedUntil( Date lockedUntil ) {
        this.lockedUntil = lockedUntil;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration( long duration ) {
        this.duration = duration;
    }

    public void save() {
        node.save();
    }

    /**
     * The actual opaque lock token given to clients
     * 
     * @return
     */
    public String getTokenId() {
        return node.getId().toString();
    }
}
