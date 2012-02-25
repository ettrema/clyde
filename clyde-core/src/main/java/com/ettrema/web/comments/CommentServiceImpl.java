package com.ettrema.web.comments;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import static com.ettrema.context.RequestContext._;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.EmptyDataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.IUser;
import com.ettrema.web.security.CurrentUserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class CommentServiceImpl implements CommentService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CommentServiceImpl.class );
    public static final String NODE_NAME_COMMENTS = "_sys_comments";

    @Override
    public List<Comment> comments( NameNode n ) {
        NameNode nComments = n.child( NODE_NAME_COMMENTS );
        if( nComments == null ) {
            return Collections.emptyList();
        } else {
            List<Comment> list = new ArrayList<>();
            for( NameNode child : nComments.children() ) {
                DataNode dn = child.getData();
                if( dn instanceof Comment ) {
                    Comment c = (Comment) dn;
                    list.add( c );
                }
            }
            return list;
        }
    }

    @Override
    public void newComment( NameNode n, String comment ) throws NotAuthorizedException {
        if( log.isTraceEnabled() ) {
            log.trace( "newComment: " + comment );
        }
        NameNode nComments = n.child( NODE_NAME_COMMENTS );
        if( nComments == null ) {
            nComments = n.add( NODE_NAME_COMMENTS, new EmptyDataNode() );
            nComments.save();
        }
        String nm = "c" + System.currentTimeMillis();
        IUser curUser = _( CurrentUserService.class ).getOnBehalfOf();
        if( curUser == null ) {
            throw new NotAuthorizedException();
        }
        Comment newComment = new Comment( curUser.getNameNodeId() );
        newComment.setComment( comment );
        NameNode nNewComment = nComments.add( nm, newComment );
        nNewComment.save();
    }

    @Override
    public UserBean getUser( UUID userNameNodeId ) {
        NameNode nUser = _( VfsSession.class ).get( userNameNodeId );
        if( nUser == null ) {
            return null;
        } else {
            DataNode dn = nUser.getData();
            if( dn instanceof IUser ) {
                IUser user = (IUser) dn;
                return new UserBeanImpl( user.getNameNode().getName(), user.getUrl(), user.getProfilePicHref() );
            } else {
                return null;
            }
        }
    }
}
