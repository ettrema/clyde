package com.bradmcevoy.web.comments;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import static com.ettrema.context.RequestContext.*;

/**
 * Represents a comment by a user on some resource.
 *
 * @author brad
 */
public class Comment implements DataNode, Serializable{
    private static final long serialVersionUID = 1L;

    private UUID id;
    private UUID userId;
    private String comment;

    private transient NameNode nameNode;

    public Comment(UUID userId) {
        this.userId = userId;
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

    public UUID userId() {
        return userId;
    }

    public UserBean getUser() {
        return _(CommentService.class).getUser(userId);
    }

    public Date getDate() {
        return nameNode.getModifiedDate();
    }

    public String getComment() {
        return comment;
    }

    public void setComment( String comment ) {
        this.comment = comment;
    }
}
