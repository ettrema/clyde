package com.ettrema.web;

import com.ettrema.http.acl.DiscretePrincipal;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.web.security.Subject;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.utils.LogUtils;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MessageFolder;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.groups.ClydeGroupHelper;
import com.ettrema.web.mail.MailProcessor;
import com.ettrema.web.security.CustomUserGroup;
import com.ettrema.web.security.PermissionRecipient;
import com.ettrema.mail.MailboxAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.mail.internet.MimeMessage;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

public class Group extends Folder implements Mailbox, CustomUserGroup, PermissionRecipient, DiscretePrincipal {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Group.class);
    private static final long serialVersionUID = 1L;
    private String emailDiscardSubjects;
    private boolean emailDisabled;
    /**
     * means that anyone cant just add themselves
     */
    private boolean secure;
    /**
     * this for logging in to email
     */
    private String password;

    public Group(Folder parentFolder, String name) {
        this(parentFolder, name, null);
    }

    public Group(Folder parentFolder, String name, String password) {
        super(parentFolder, name);
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new User(parent, newName);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        this.emailDisabled = InitUtils.getBoolean(el, "emailDisabled");
        this.secure = InitUtils.getBoolean(el, "secure");
        this.password = InitUtils.getValue(el, "emailPassword");
        this.emailDiscardSubjects = InitUtils.getValue(el, "emailDiscardSubject");
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        InitUtils.setBoolean(e2, "emailDisabled", emailDisabled);
        InitUtils.setBoolean(e2, "secure", secure);
        InitUtils.setString(e2, "emailPassword", password);
        InitUtils.setString(e2, "emailDiscardSubject", emailDiscardSubjects);
    }

    @Override
    public boolean isEmailDisabled() {
        return emailDisabled;
    }

    public void setEmailDisabled(boolean emailDisabled) {
        this.emailDisabled = emailDisabled;
    }

    public String getEmailAddressText() {
        Host h = this.getHost();
        String s = this.getName() + "@" + h.getName();
        return s;
    }

    @Override
    public MessageFolder getInbox() {
        Folder f = getEmailFolder();
        if (f == null) {
            return null;
        }
        return new ClydeMessageFolder(f);
    }

    public Folder getEmailFolder() {
        return getEmailFolder(false);
    }

    public Folder getEmailFolder(boolean create) {
        String emailFolderName = "email";
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
            } catch (NotAuthorizedException ex) {
                throw new RuntimeException(ex);
            } catch (BadRequestException ex) {
                throw new RuntimeException(ex);
            }
        }
        return emailFolder;
    }

    /**
     * Send to everone in the list
     *
     * @param mm
     */
    @Override
    public void storeMail(MimeMessage mm) {
        if (emailDisabled) {
            log.warn("Email received to group with disabled email. Ignoring");
        }
        MailProcessor mailProc = requestContext().get(MailProcessor.class);
        if (mailProc == null) {
            throw new RuntimeException("No " + MailProcessor.class.getCanonicalName() + " is configured. Check catalog.xml");
        }
        MailboxAddress groupAddress = MailboxAddress.parse(this.getEmailAddressText());
        mailProc.handleGroupEmail(mm, getEmailFolder(true), requestContext(), getMembers(), groupAddress, emailDiscardSubjects);
        this.commit();
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean authenticate(String password) {
        if (this.password == null) {
            return password == null;
        } else {
            return this.password.equals(password);
        }
    }

    @Override
    public boolean authenticateMD5(byte[] passwordHash) {
        try {
            if (this.password == null) {
                return false;
            }
            String s = this.password;
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            byte[] actual = digest.digest(s.getBytes());
            return Arrays.equals(actual, passwordHash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getSubjectName() {
        return getName();
    }

    @Override
    public boolean isInGroup(Subject subject) {
        if (subject instanceof IUser) {
            return _(ClydeGroupHelper.class).isInGroup((IUser) subject, this);
        } else {
            return false;
        }
    }

    public List<User> getMembers() {
        // TODO: if the subject is another gruop probably should iterate over it and add its members
        List<Subject> subjects = _(ClydeGroupHelper.class).getMembers(this);
        List<User> list = new ArrayList<>();
        for (Subject r : subjects) {
            if (r instanceof User) {
                User u = (User) r;
                list.add(u);
            }
        }
        return list;
    }

    public String getPassword() {
        return password;
    }

    public String getEmailDiscardSubjects() {
        return emailDiscardSubjects;
    }

    public void setEmailDiscardSubjects(String emailDiscardSubjects) {
        this.emailDiscardSubjects = emailDiscardSubjects;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    @Override
    public boolean appliesTo(Subject user) {
        LogUtils.trace(log, "appliesTo: group", getName());
        List<User> members = this.getMembers();
        if (members.isEmpty()) {
            log.trace("appliesTo: no members in this group");
            return false;
        } else {
            for (User member : members) {
                LogUtils.trace(log, "appliesTo: member: ", member.getClass(), member.getName());
                if (member.appliesTo(user)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isOrContains(Subject s) {
        return appliesTo(s);
    }

    @Override
    public PrincipleId getIdenitifer() {
        return new HrefPrincipleId(this.getHref());
    }

    @Override
    public RelationalNameNode getPermissionsNameNode() {
        return permissions(true).getNameNode();
    }
}
