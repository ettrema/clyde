package com.ettrema.web.comments;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import static com.ettrema.context.RequestContext.*;
import com.ettrema.web.BaseResource;

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
    private Date commentDate;

    private transient NameNode nameNode;

    public Comment(UUID userId) {
        this.userId = userId;
    }

    public NameNode getNameNode() {
        return nameNode;
    }

    @Override
    public void setId( UUID id ) {
        this.id = id;
    }

    /**
     * The data node id
     * 
     * @return 
     */
    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void init( NameNode nameNode ) {
        this.nameNode = nameNode;
    }

    @Override
    public void onDeleted( NameNode nameNode ) {
        
    }

    public UUID userId() {
        return userId;
    }

    public UserBean getUser() {
        return _(CommentService.class).getUser(userId);
    }

    public Date getDate() {
        return commentDate;
    }
    
    public void setDate(Date dt) {
        commentDate = dt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment( String comment ) {
        this.comment = comment;
    }
    
    public BaseResource page() {
        return (BaseResource) nameNode.getParent().getParent().getData();
    }
}
