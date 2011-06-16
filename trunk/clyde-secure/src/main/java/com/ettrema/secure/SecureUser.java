package com.ettrema.secure;

import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.ClydeMessageFolder;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.mail.MailProcessor;
import com.ettrema.mail.MessageFolder;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jdom.Element;

/**
 *
 */
public class SecureUser extends Folder implements IUser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SecureUser.class);
    private static final long serialVersionUID = 1L;
    private boolean emailDisabled;
    private String externalEmail;
    private String passwordHash;
    private String binaryKey;

    public SecureUser(Folder parentFolder, String name) {
        this(parentFolder, name, null);
    }

    public SecureUser(Folder parentFolder, String name, String password) {
        super(parentFolder, name);
        storePassword(password);
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        SecureUser uNew = (SecureUser) super.copyInstance(parent, newName);
        // todo
        return uNew;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new SecureUser(parent, newName);
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        InitUtils.setBoolean(e2, "emailDisabled", emailDisabled);
        InitUtils.setString(e2, "email", externalEmail);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        this.emailDisabled = InitUtils.getBoolean(el, "emailDisabled");
        this.externalEmail = InitUtils.getValue(el, "email");
    }

    @Override
    public boolean isEmailDisabled() {
        return emailDisabled;
    }

    public void setEmailDisabled(boolean emailDisabled) {
        this.emailDisabled = emailDisabled;
    }

    @Override
    public boolean authenticate(String password) {
        return checkPassword(password);
    }

    /**
     *
     * @return - the email address for this user on this domain. NOT their specified
     *  external email
     */
    public Address getEmailAddress() {
        Host h = this.getHost();
        String s = this.getName() + "@" + h.getName();
        Address add;
        try {
            add = new InternetAddress(s);
        } catch (AddressException ex) {
            throw new RuntimeException(ex);
        }
        return add;
    }

    @Override
    public MessageFolder getInbox() {
        log.debug("getInbox");
        Folder f = getEmailFolder();
        if (f == null) {
            log.warn("no inbox for: " + this.getName());
            return null;
        }
        return new ClydeMessageFolder(f);
    }

    public Folder getEmailFolder() {
        return getEmailFolder(false);
    }

    public Folder getEmailFolder(boolean create) {
        String emailFolderName = "inbox";
        Folder emailFolder = getMailFolder(emailFolderName, create);
        return emailFolder;
    }

    @Override
    public MessageFolder getMailFolder(String name) {
        Folder f = getMailFolder(name, false);
        if (f == null) {
            return null;
        }
        return new ClydeMessageFolder(f);
    }

    public Folder getMailFolder(String name, boolean create) {
        String emailFolderName = "email_" + name;
        Folder emailFolder = getSubFolder(emailFolderName);
        if (emailFolder == null && create) {
            try {
                emailFolder = (Folder) createCollection(emailFolderName, false);
            } catch (ConflictException ex) {
                throw new RuntimeException(ex);
            }
        }
        return emailFolder;

    }

    @Override
    public void storeMail(MimeMessage mm) {
        MailProcessor mailProc = requestContext().get(MailProcessor.class);
        if (mailProc == null) {
            throw new RuntimeException("No " + MailProcessor.class.getCanonicalName() + " is configured. Check catalog.xml");
        }
        String emailRecip = getExternalEmailText();
        if (emailRecip == null) {
            Folder destFolder = getEmailFolder(true);
            mailProc.persistEmail(mm, destFolder, requestContext());
            this.commit();
        } else {
            mailProc.forwardEmail(mm, emailRecip, requestContext());
        }
    }

    boolean checkPassword(String password) {
        PasswordManager mgr = requestContext().get(PasswordManager.class);
        if( mgr == null ) throw new RuntimeException("No PasswordManager is defined");
        return mgr.checkPassword(password, passwordHash);
    }

    /**
     * Required for PermissionRecipient. Just returns this.
     *
     * @return - this
     */
    @Override
    public IUser getUser() {
        return this;
    }

    public String getExternalEmailText() {
        return externalEmail;
    }

    public void setExternalEmail(String externalEmail) {
        this.externalEmail = externalEmail;
    }

    public boolean authenticateMD5(byte[] passwordHash) {
        throw new UnsupportedOperationException("Not supported for a SecureUser.");
    }

    private void storePassword(String password) {
        PasswordManager mgr = requestContext().get(PasswordManager.class);
        if( mgr == null ) throw new RuntimeException("No PasswordManager is defined");
        passwordHash = mgr.digestPassword(password);
    }

    public String getPlainBinaryKey(String password) {
        return null;
    }
}
