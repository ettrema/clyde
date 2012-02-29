package com.ettrema.web;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.util.Map;
import com.ettrema.http.acl.Principal.PrincipleId;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.vfs.Relationship;
import com.ettrema.web.security.BeanProperty;
import com.ettrema.web.security.Permissions;
import java.util.Collection;
import com.ettrema.web.security.PasswordStorageService;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.security.PermissionChecker;
import com.ettrema.web.security.Subject;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.property.BeanPropertyAccess;
import java.util.ArrayList;
import java.util.UUID;
import com.ettrema.web.wall.Wall;
import com.ettrema.web.wall.WallItem;
import com.ettrema.web.wall.WallService;
import java.util.Collections;
import java.util.List;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.mail.MessageFolder;
import com.ettrema.media.MediaLogService;
import com.ettrema.media.MediaLogService.MediaLog;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.Text;
import com.ettrema.web.groups.GroupService;
import com.ettrema.web.groups.RelationalGroupHelper;
import com.ettrema.web.mail.MailProcessor;
import com.ettrema.web.security.CookieAuthenticationHandler;
import com.ettrema.web.security.CurrentUserService;
import com.ettrema.web.security.UserGroup;
import java.util.HashMap;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.groups.ClydeGroupHelper;
import java.util.logging.Level;
import java.util.logging.Logger;

@BeanPropertyResource("clyde")
public class User extends Folder implements IUser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(User.class);
    private static final long serialVersionUID = 1L;
    public static final String REL_SHARED = "_sys_isshared";
    private Text password;
    private boolean emailDisabled;
    private boolean accountDisabled;
    private List<String> accessKeys;
    private String profilePicHref;

    public User(Folder parentFolder, String name) {
        this(parentFolder, name, null);
    }

    public User(Folder parentFolder, String name, String password) {
        super(parentFolder, name);
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        User uNew = (User) super.copyInstance(parent, newName);
        return uNew;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new User(parent, newName);
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        InitUtils.setBoolean(e2, "accountDisabled", accountDisabled);
        InitUtils.setBoolean(e2, "emailDisabled", emailDisabled);

        Element elEmail = new Element("email");
        elEmail.setText(getExternalEmailText());
        e2.addContent(elEmail);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        password = (Text) this.componentMap.get("password");

        // TODO: ??
        String s = el.getAttributeValue("groupNames");

        this.emailDisabled = InitUtils.getBoolean(el, "emailDisabled");
        this.accountDisabled = InitUtils.getBoolean(el, "accountDisabled");
        Element elEmail = el.getChild("email");
        if (elEmail != null) {
            String newEmail = elEmail.getText();
            setExternalEmailText(newEmail);
        }
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
        return _(PasswordStorageService.class).checkPassword(this, password);
    }

    @Override
    public boolean authenticateMD5(byte[] passwordHash) {
        return _(PasswordStorageService.class).checkPasswordMD5(this, passwordHash);
    }

    /**
     * Always returns a blank. Just used to make password a bean property, but
     * only the setter is useful
     *
     * @return
     */
    public String getPassword() {
        return "";
    }

    public void setPassword(String newPassword) {
        _(PasswordStorageService.class).setPasswordValue(this, newPassword);
    }

    public boolean checkPassword(String password) {
        return _(PasswordStorageService.class).checkPassword(this, password);
    }

    public boolean checkPassword(DigestResponse digestRequest) {
        return _(PasswordStorageService.class).checkPassword(this, digestRequest);
    }

    /**
     *         // note that this can cause an error sometimes, eg if the user name
    // has a space in it
    
     * 
     * @return - the email address for this user on this domain. NOT their specified
     *  external email
     */
    @Override
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
    public ClydeMessageFolder getInbox() {
        log.debug("getInbox");
        Folder f = getEmailFolder();
        if (f == null) {
            log.warn("no inbox for: " + this.getName());
            return null;
        }
        return new ClydeMessageFolder(f);
    }

    @Override
    public Folder getEmailFolder() {
        return getEmailFolder(false);
    }

    @Override
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

    @Override
    public Folder getMailFolder(String name, boolean create) {
        try {
            return _(MailProcessor.class).getMailFolder(this, name, create);
        } catch (ConflictException | NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
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

    /**
     * 
     * @return - the user's specified external email address as a string. Null if not specified
     */
    @Override
    public String getExternalEmailText() {
        String s = getExternalEmailTextV2("default");
        if (s != null) {
            return s;
        } else {
            ComponentValue cvEmail = this.getValues().get("email");
            if (cvEmail == null || cvEmail.getValue() == null) {
                return null;
            }
            s = cvEmail.getValue().toString();
            if (s.trim().length() == 0) {
                return null;
            }
            return s;
        }
    }

    public void setExternalEmailText(String email) {
        ComponentValue cvEmail = this.getValues().get("email");
        if (cvEmail == null) {
            cvEmail = new ComponentValue("email", this);
            cvEmail.init(this);
            cvEmail.setValue(email);
            this.getValues().add(cvEmail);
        } else {
            cvEmail.setValue(email);
        }
        setExternalEmailTextV2("default", email);
    }

    @Override
    public boolean is(String type) {
        if ("user".equals(type)) {
            return true;
        }
        if (isInGroup(type)) {
            return true;
        }
        return super.is(type);
    }

    @Override
    public boolean isInGroup(String groupName) {
        UserGroup group = _(GroupService.class).getGroup(this, groupName);
        if (group != null) {
            boolean b = group.isInGroup(this);
            LogUtils.trace(log, "isInGroup: group=", groupName, "result=", b);
            return b;
        } else {
            LogUtils.trace(log, "isInGroup: group not found", groupName); // can be expected, since is called from is()
            return false;
        }
    }

    public boolean isInGroup(Group g) {
        return g.isInGroup(this);
    }

    public String getMobileNumber() {
        ComponentValue cvMobile = this.getValues().get("mobile");
        if (cvMobile == null || cvMobile.getValue() == null) {
            return null;
        }
        String s = cvMobile.getValue().toString();
        if (s.trim().length() == 0) {
            return null;
        }
        return s;
    }

    @Override
    public String getSubjectName() {
        return getName();
    }

    public void addToGroup(String groupName) {
        Group g = (Group) _(RelationalGroupHelper.class).getGroup(this, groupName);
        if (g == null) {
            throw new RuntimeException("Group not found: " + groupName);
        }
        addToGroup(g);
    }

    public void addToGroup(Group g) {
        _(RelationalGroupHelper.class).addToGroup(this, g);
    }

    public void removeFromGroup(String groupName) {
        if (groupName == null || groupName.length() == 0) {
            throw new IllegalArgumentException("Group name is empty or null");
        }
        RelationalGroupHelper groupService = _(RelationalGroupHelper.class);
        UserGroup userGroup = groupService.getGroup(this, groupName);
        if (userGroup == null) {
            throw new NullPointerException("Group not found: " + groupName);
        } else if (userGroup instanceof Group) {
            removeFromGroup((Group) userGroup);
        } else {
            throw new RuntimeException("Cant remove group type: " + userGroup.getClass());
        }
    }
    
    public List<Group> getGroups() {
        return _(ClydeGroupHelper.class).getGroups(this);
    }

    public void removeFromGroup(Group group) {
        RelationalGroupHelper groupService = _(RelationalGroupHelper.class);
        if (group.isInGroup(this)) {
            groupService.removeFromGroup(this, group);
        }
    }

    public List<WallItem> getWall() {
        log.warn("getWall");
        Wall wall = _(WallService.class).getUserWall(this, false);
        if (wall == null) {
            return Collections.emptyList();
        } else {
            List<WallItem> list = wall.getItems();
            log.warn("wall items: " + list.size());
            return list;
        }
    }

    @BeanPropertyAccess(false)
    public List<String> getAccessKeys() {
        if (accessKeys == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList(accessKeys);
        }
    }

    /**
     * Creates a new access key and adds it to the list on this host
     *
     * @return
     */
    public String createNewAccessKey() {
        UUID newId = UUID.randomUUID();
        if (accessKeys == null) {
            accessKeys = new ArrayList<>();
        }
        accessKeys.add(newId.toString());
        return newId.toString();
    }

    /**
     * Makes this the currently logged in user by setting the CookieAuthenticationHandler
     * cookies.
     *
     * Requires CookieAuthenticationHandler to be in the catalog
     *
     */
    public void login() {
        if (accountDisabled) {
            log.info("Cant login with a disabled account");
        }
        log.trace("do login");
        Request req = _(CurrentRequestService.class).request();
        _(CookieAuthenticationHandler.class).setLoginCookies(this, req);
    }

    @Override
    public boolean appliesTo(Subject user) {
        log.trace("appliesTo");
        if (user instanceof User) {
            User u = (User) user;
            return u.getNameNodeId().equals(this.getNameNodeId());
        } else {
            return false;
        }
    }

    @Override
    public boolean isOrContains(Subject s) {
        return appliesTo(s);
    }

    /**
     * Checks to see if the given user is the same as this user
     * 
     * Is an alias for appliesTo
     *
     * @param iuser
     * @return
     */
    public boolean is(IUser iuser) {
        return appliesTo(iuser);
    }

    public boolean isSysAdmin() {
        return _(PermissionChecker.class).hasRole(Role.SYSADMIN, this, null);
    }

    /**
     * Can this user author the given resource
     * 
     * @param r
     * @return
     */
    @Override
    public boolean canAuthor(Resource r) {
        Auth auth = null;
        Request req = _(CurrentRequestService.class).request();
        if (req != null) {
            auth = req.getAuthorization();
        }
        boolean b = _(PermissionChecker.class).hasRole(Role.AUTHOR, r, auth);
        return b;
    }

    public boolean hasRole(String role, Resource res) {
        Role r = null;
        try {
            r = Role.valueOf(role);
        } catch (Exception e) {
            log.error("invalid role: " + role, e);
            return false;
        }
        Auth auth;
        if (HttpManager.request() == null) {
            log.trace("no HTTP request, so look for current user");
            IUser user = _(CurrentUserService.class).getSecurityContextUser();
            if (user == null) {
                log.trace("found a current user so wrap in Auth object");
                auth = new Auth(user.getName(), user);
            } else {
                log.trace("no current user");
                auth = null;
            }
        } else {
            auth = HttpManager.request().getAuthorization();
        }

        return _(PermissionChecker.class).hasRole(r, res, auth);
    }

    /**
     * Access legacy password component
     * 
     * @return
     */
    public Text passwordComponent() {
        return this.password;
    }

    public void setPasswordComponent(Text text) {
        this.password = text;
    }

    public void copyPasswordFrom(User user) {
        String pwd = _(PasswordStorageService.class).getPasswordValue(user);
        setPassword(pwd);
    }

    public boolean isAccountDisabled() {
        return accountDisabled;
    }

    public void setAccountDisabled(boolean accountDisabled) {
        this.accountDisabled = accountDisabled;
    }

    public List<MediaLog> getMedia() {
        return _(MediaLogService.class).getMedia(this, null, 0);
    }

    public List<MediaLog> getMedia(int page) {
        return _(MediaLogService.class).getMedia(this, null, page);
    }

    public List<MediaLogService.AlbumLog> getAlbums() {
        return _(MediaLogService.class).getAlbums(this, null);
    }

    public List<MediaLogService.AlbumYear> getAlbumTimeline() {
        return _(MediaLogService.class).getAlbumTimeline(this, null);
    }

    public List<MediaLogService.AlbumYear> albumTimeline(String path) {
        return _(MediaLogService.class).getAlbumTimeline(this, path);
    }

    @Override
    public PrincipleId getIdenitifer() {
        return new HrefPrincipleId(this.getHref());
    }

    @BeanProperty
    public Collection<SharedWithMe> getSharedWithMe() {
        System.out.println("getShares");
        Permissions perms = this.permissions();
        Map<Resource, SharedWithMe> map = new HashMap<Resource, SharedWithMe>();
        if (perms != null) {
            List<Relationship> relsViewer = perms.getNameNode().findToRelations(Role.VIEWER.toString());
            for (Relationship r : relsViewer) {
                BaseResource shared = (BaseResource) r.from().getParent().getData();
                User sharingUser = shared.getCreator();
                SharedWithMe sharedWithMe = map.get(shared);
                if (sharedWithMe == null) {
                    sharedWithMe = new SharedWithMe(sharingUser, shared);
                    map.put(shared, sharedWithMe);
                }
            }
            List<Relationship> relsAuthor = perms.getNameNode().findToRelations(Role.AUTHOR.toString());
            for (Relationship r : relsAuthor) {
                BaseResource shared = (BaseResource) r.from().getParent().getData();
                User sharingUser = shared.getCreator();

                SharedWithMe sharedWithMe = map.get(shared);
                if (sharedWithMe == null) {
                    sharedWithMe = new SharedWithMe(sharingUser, shared);
                    map.put(shared, sharedWithMe);
                }
                sharedWithMe.setWritable(true);
            }
        }
        List<SharedWithMe> list = new ArrayList<SharedWithMe>();
        list.addAll(map.values());
        return list;
    }
    
    public List<Folder> getShared() {
        List<Relationship> rels = this.getNameNode().findToRelations(REL_SHARED);
        if (rels == null || rels.isEmpty()) {
            return null;
        } else {
            List<Folder> list = new ArrayList<Folder>();
            for (Relationship rel : rels) {
                NameNode nFrom = rel.from();
                if (nFrom == null) {
                    log.warn("from node does not exist");
                    return null;
                } else {
                    DataNode dnFrom = nFrom.getData();
                    if (dnFrom == null) {
                        log.warn("to node has no data");
                    } else {
                        if (dnFrom instanceof Folder) {
                            Folder cr = (Folder) dnFrom;
                            list.add(cr);
                        } else {
                            log.warn("from node is not a: " + Folder.class + " is a: " + dnFrom.getClass());
                        }
                    }
                }
            }
            return list;
        }
    }    
        

    @Override
    public RelationalNameNode getPermissionsNameNode() {
        return permissions(true).getNameNode();
    }

    @Override
    public String getProfilePicHref() {
        return profilePicHref;
    }

    public void setProfilePicHref(String profilePicHref) {
        this.profilePicHref = profilePicHref;
    }
    
    

    public static class SharedWithMe {

        private final Subject subject;
        private boolean writable;
        private final Resource resource;

        public SharedWithMe(Subject subject, Resource resource) {
            this.subject = subject;
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }

        public Subject getSubject() {
            return subject;
        }

        public boolean isWritable() {
            return writable;
        }

        public void setWritable(boolean writable) {
            this.writable = writable;
        }
    }
}
