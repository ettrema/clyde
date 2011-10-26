package com.ettrema.binary;

import com.ettrema.web.IUser;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.Serializable;
import java.util.UUID;

/**
 * Is a DataNode representing a version
 *
 */
public class Version implements DataNode, Serializable, VersionDescriptor{

    private static final long serialVersionUID = 1L;

    private transient NameNode nameNode;

    private UUID id;
    private long crc;
    private UUID userId;
    private long contentLength;

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

    public NameNode nameNode() {
        return nameNode;
    }

    void setCrc(long crc) {
        this.crc = crc;
    }

    public long getCrc() {
        return crc;
    }

    void save() {
        this.nameNode.save();
    }

    void setUserId(UUID nameNodeId) {
        this.userId = nameNodeId;
    }

    public UUID getUserId() {
        return userId;
    }

    public long getContentLength() {
        return contentLength;
    }

    void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getVersionNum() {
        return this.nameNode.getName();
    }

    public String getUserName() {
        VfsSession vfs = RequestContext.getCurrent().get(VfsSession.class);
        NameNode nn = vfs.get(this.userId);
        if( nn == null ) {
            return null;
        } else {
            DataNode dn = nn.getData();
            if( dn == null ) {
                return null;
            } else if( dn instanceof IUser) {
                IUser user = (IUser) dn;
                return user.getNameNode().getName();
            }else {
                return null;
            }
        }
    }
}
