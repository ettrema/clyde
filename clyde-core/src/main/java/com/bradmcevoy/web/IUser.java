package com.bradmcevoy.web;

import com.bradmcevoy.web.security.PermissionRecipient;
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

    Folder getEmailFolder();

    Folder getEmailFolder(boolean create);

    /**
     *
     * @return - the user's specified external email address as a string. Null if not specified
     */
    String getExternalEmailText();

    MessageFolder getInbox();

    MessageFolder getMailFolder(String name);

    Folder getMailFolder(String name, boolean create);

    boolean is(String type);

    boolean isEmailDisabled();

    void storeMail(MimeMessage mm);
    
    String getHref();

}
