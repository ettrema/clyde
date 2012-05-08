package com.ettrema.web.comments;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import static com.ettrema.context.RequestContext._;
import com.ettrema.utils.CurrentDateService;
import com.ettrema.vfs.*;
import com.ettrema.web.IUser;
import com.ettrema.web.security.CurrentUserService;
import java.util.*;

/**
 *
 * @author brad
 */
public class CommentServiceImpl implements CommentService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CommentServiceImpl.class );
    public static final String NODE_NAME_COMMENTS = "_sys_comments";
    
    private final CurrentUserService currentUserService;
    
    private final CurrentDateService currentDateService;
    
    private final CommentDao commentDao;

    public CommentServiceImpl(CurrentUserService currentUserService, CurrentDateService currentDateService, CommentDao commentDao) {
        this.currentUserService = currentUserService;
        this.currentDateService = currentDateService;
        this.commentDao = commentDao;
    }       

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
        IUser curUser = currentUserService.getOnBehalfOf();
        if( curUser == null ) {
            throw new NotAuthorizedException();
        }
        Date now = currentDateService.getNow();
        newComment(n, comment, now, curUser);
    }
    
    @Override
    public void newComment( NameNode n, String comment, Date commentDate, IUser curUser ) throws NotAuthorizedException {
        if( log.isTraceEnabled() ) {
            log.trace( "newComment: " + comment );
        }
        NameNode nComments = n.child( NODE_NAME_COMMENTS );
        if( nComments == null ) {
            nComments = n.add( NODE_NAME_COMMENTS, new EmptyDataNode() );
            nComments.save();
        }
        String nm = "c" + System.currentTimeMillis();
        Comment newComment = new Comment( curUser.getNameNodeId() );
        newComment.setComment( comment );
        newComment.setDate(new Date());
        NameNode nNewComment = nComments.add( nm, newComment );
        nNewComment.save();
        
        commentDao.insert(newComment);
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

    @Override
    public void deleteAll(RelationalNameNode nameNode) {
        NameNode nComments = nameNode.child( NODE_NAME_COMMENTS );
        if( nComments == null ) {
            return ;
        } else {
            nComments.delete();
        }
    }
}
