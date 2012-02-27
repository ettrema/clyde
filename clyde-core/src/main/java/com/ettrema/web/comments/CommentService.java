package com.ettrema.web.comments;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.web.IUser;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public interface CommentService {
    List<Comment> comments(NameNode n);

    /**
     * Create a new comment from the current user and for the current date
     * 
     * @param n
     * @param comment
     * @throws NotAuthorizedException 
     */
    void newComment(NameNode n, String comment) throws NotAuthorizedException;
    
    void newComment(NameNode n, String comment, Date commentDate, IUser user) throws NotAuthorizedException;

    UserBean getUser(UUID userNameNodeId);

    /**
     * Remove all comments from the given namenode
     * 
     * @param nameNode 
     */
    void deleteAll(RelationalNameNode nameNode);
}
