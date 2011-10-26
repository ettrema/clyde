package com.ettrema.web;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.security.PermissionRecipient;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MessageFolder;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author brad
 */
public interface IUser extends Mailbox, PermissionRecipient {

    /**
     *
     * @return - the email address for this user on this domain. NOT their specified
     * external email
     */
    Address getEmailAddress();

    CollectionResource getEmailFolder();

    CollectionResource getEmailFolder(boolean create);

    /**
     *
     * @return - the user's specified external email address as a string. Null if not specified
     */
    String getExternalEmailText();

    MessageFolder getInbox();

    MessageFolder getMailFolder(String name);

    CollectionResource getMailFolder(String name, boolean create);

    boolean is(String type);

    boolean isEmailDisabled();

    void storeMail(MimeMessage mm);

    /**
     * The fully qualified url of this user, including the protocol. Use getUrl
     * for an absolute path (note the naming error)
     *
     * @return
     */
    String getHref();

    /**
     * The name of the resource which identifies the user. This is the local name
     * within the folder
     *
     * @return
     */
    String getName();

    /**
     * If this user is a member of the given group
     *
     * @param groupName
     * @return
     */
    boolean isInGroup( String groupName );
    
    /**
     * Can this user author the given resource
     * 
     * @param r
     * @return
     */    
    boolean canAuthor(Resource r);

}
