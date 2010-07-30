package com.bradmcevoy.web.comments;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.vfs.NameNode;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public interface CommentService {
    List<Comment> comments(NameNode n);

    void newComment(NameNode n, String comment) throws NotAuthorizedException;

    UserBean getUser(UUID userNameNodeId);
}
