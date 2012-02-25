package com.ettrema.web;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.http.acl.DiscretePrincipal;
import com.ettrema.web.security.PermissionRecipient;
import com.ettrema.mail.Mailbox;
import com.ettrema.vfs.RelationalNameNode;
import java.util.UUID;
import javax.mail.Address;

/**
 *
 * @author brad
 */
public interface IUser extends Mailbox, PermissionRecipient, DiscretePrincipal {

    /**
     *
     * @return - the email address for this user on this domain. NOT their
     * specified external email
     */
    Address getEmailAddress();

    CollectionResource getEmailFolder();

    CollectionResource getEmailFolder(boolean create);

    /**
     *
     * @return - the user's specified external email address as a string. Null
     * if not specified
     */
    String getExternalEmailText();

    CollectionResource getMailFolder(String name, boolean create);

    boolean is(String type);

    /**
     * The fully qualified url of this user, including the protocol. Use getUrl
     * for an absolute path (note the naming error)
     *
     * @return
     */
    String getHref();
    
    /**
     * A path identifying the user, from the server
     * 
     * @return 
     */
    String getUrl();

    /**
     * The name of the resource which identifies the user. This is the local
     * name within the folder
     *
     * @return
     */
    @Override
    String getName();

    /**
     * If this user is a member of the given group
     *
     * @param groupName
     * @return
     */
    boolean isInGroup(String groupName);

    /**
     * Can this user author the given resource
     *
     * @param r
     * @return
     */
    boolean canAuthor(Resource r);
    
    /**
     * Return the href to the user's profile pic, or null if none is
     * available
     * 
     * @return 
     */
    String getProfilePicHref();

    RelationalNameNode getNameNode();

    UUID getNameNodeId();
}
