package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.security.UserLocator;
import com.ettrema.web.stats.StatsService;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.aws.BucketOwner;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;
import com.ettrema.underlay.UnderlayVector;
import java.util.ArrayList;

@BeanPropertyResource("clyde")
public class Host extends Web implements BucketOwner {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Host.class);
    private static final long serialVersionUID = 1L;
    Path hostPath;
    private boolean disabled;
    private List<UnderlayVector> underlayVectors;

    public Host(Folder parent, String name) {
        super(parent, name);
        this.getTemplate();
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        Host hNew = (Host) super.copyInstance(parent, newName);
        hNew.hostPath = this.hostPath;
        return hNew;
    }

    @Override
    public boolean is(String type) {
        if (type.equalsIgnoreCase("host")) {
            return true;
        }
        return super.is(type);
    }

    @Override
    public void save() {
        String name = getName();
        System.out.println("Host save: " + name + " - " + name.length());
        if( !name.equals(name.trim())) {
            throw new RuntimeException("Host name starts or ends with whitepsace");
        }
        if( name.length() == 0 ) {
            throw new RuntimeException("Host name is empty");
        }
        if( name.contains(" ")) {
            throw new RuntimeException("Host name contains space(s)");
        }
        super.save();
    }

    
    
    @Override
    public List<Template> getAllowedTemplates() {
        if (templateSpecs == null) {
            return TemplateSpecs.findApplicable(this);
        } else {
            return templateSpecs.findAllowed(this);
        }
    }

    /**
     * Sets the path to the host that this will be an alias for. Does not save
     *
     * @param pAliasPath
     */
    public void setAliasPath(Path pAliasPath) {
        this.hostPath = pAliasPath;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new Host(parent, newName);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        String s = el.getAttributeValue("hostPath");
        if (s != null && s.trim().length() > 0) {
            hostPath = Path.path(s.trim());
        }
        this.disabled = InitUtils.getBoolean(el, "disabled");
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        if (hostPath != null) {
            e2.setAttribute("hostPath", hostPath.toString());
        }
        InitUtils.setBoolean(e2, "disabled", disabled);
    }

    public Host getAliasedHost() {
        if (hostPath == null) {
            return null;
        }
        NameNode nn = vfs().find(hostPath);
        if (nn == null) {
            log.warn("Did not fina alised host name node: " + hostPath);
            return null;
        }
        Resource res = (Resource) nn.getData();
        if (res == null) {
            log.warn("Did not find aliased host data node: " + hostPath + " in namenode: " + nn.getId());
            return null;
        } else if (res instanceof Host) {
            return (Host) res;
        } else {
            log.warn("object is not a host: " + hostPath + ".is a:" + res.getClass().getName());
            return null;
        }
    }

    @Override
    public Resource child(String name) {
        if (hostPath != null) {
            Host host = getAliasedHost();
            if (host != null) {
                return host.child(name);
            }
        }
        return super.child(name);
    }

    @Override
    public List<? extends Resource> getChildren() {
        if (hostPath != null) {
            Host host = getAliasedHost();
            if (host != null) {
                return host.getChildren();
            }
        }
        return super.getChildren();
    }

    @Override
    public CollectionResource createCollection(String newName, boolean commit) throws ConflictException, NotAuthorizedException, BadRequestException {
        if (hostPath != null) {
            Host host = getAliasedHost();
            if (host != null) {
                return host.createCollection(newName, commit);
            }
        }
        return super.createCollection(newName, commit);
    }

    @Override
    public Resource createNew(String newName, InputStream in, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        if (hostPath != null) {
            Host host = getAliasedHost();
            if (host != null) {
                return host.createNew(newName, in, length, contentType);
            }
        }
        return super.createNew(newName, in, length, contentType);
    }

    boolean isAlias() {
        return (hostPath != null);
    }

    @Override
    public Host getHost() {
        return this;
    }

    public Folder getUsers() {
        return getUsers(false);
    }

    public Folder getUsers(boolean create) {
        Folder users = (Folder) this.child("users");
        if (users == null) {
            if (create) {
                try {
                    users = (Folder) this.createCollection("users", false);
                    users.templateSpecs.add("+user");
                    users.templateSpecs.add("+group");
                    users.templateSpecs.add("-*");
                } catch (ConflictException | NotAuthorizedException | BadRequestException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return users;

    }

    public User findUser(String name) {
        UserLocator userLocator = _(UserLocator.class);
        return userLocator.findUser(this, name);
    }

    public User findByEmail(String s) {
        // look for a matching email address       
        UserLocator userLocator = _(UserLocator.class);
        MailboxAddress add = userLocator.parse(s);
        if (add != null) {
            User user = userLocator.findUserByEmail(add, this);
            if (user != null) {
                return user;
            }
        }
        return null;
    }

    public List<User> searchForUsers(String s) {
        UserLocator userLocator = _(UserLocator.class);
        return userLocator.search(s, this);
    }

    /**
     * Authenticates against this particular domain (non recursive)
     *
     *
     * @param user
     * @param password
     * @return - null if not authenticated. otherwise, the user object
     */
    public User doAuthenticate(String user, String password) {
        User userRes = findUser(user);

        if (userRes == null) {
            //log.warn("user not found: " + user + " in domain: " + getName());
            return null;
        }

        // have found a user of that name, so authenticate it
        boolean b = userRes.checkPassword(password);
        if (b) {
            return userRes;
        } else {
            return null;
        }
    }

    public User doAuthenticate(String user, DigestResponse digestRequest) {
        User userRes = findUser(user);

        if (userRes == null) {
            log.debug("user not found: " + user + " in domain: " + getName());
            return null;
        }

        // have found a user of that name, so authenticate it
        boolean b = userRes.checkPassword(digestRequest);
        if (b) {
            return userRes;
        } else {
            log.warn("checkPassword failed in host: " + this.getName());
            return null;
        }
    }

    @Override
    protected void afterSave() {
        super.afterSave();
        Folder users = getUsers(true);
        users.save();
    }

    public Group findGroup(String groupName) {
        Resource r = getUsers().child(groupName);
        if (r == null) {
            log.trace("findGroup: no resource named: " + groupName);
            return null;
        } else if (r instanceof Group) {
            Group g = (Group) r;
            return g;
        } else {
            log.warn("findGroup: Found a resource which is not a group. Is a: " + r.getClass() + " name=" + r.getName());
            return null;
        }
    }

    public List<Group> getGroups() {
        List<Group> list = new ArrayList<>();
        for (Resource r : getUsers().getChildren()) {
            if (r instanceof Group) {
                list.add((Group) r);
            }
        }
        return list;
    }

    public User createUser(String name, String pwd) {
        return createUser(name, pwd, null, null);
    }

    /**
     * Create and save a user. Will create the user from the templateName if
     * given and it can be found
     *
     * @param name
     * @param pwd
     * @param templateName
     * @return
     */
    public User createUser(String name, String pwd, Group group, String templateName) {
        Folder users = getUsers(true);
        if (users.childExists(name)) {
            throw new RuntimeException("Resource already exists: " + name);
        }
        ITemplate template = null;
        if (templateName != null && templateName.length() > 0) {
            template = users.getTemplate(templateName);
        }
        User user;
        if (template == null) {
            user = new User(users, name);
        } else {
            BaseResource newRes = template.createPageFromTemplate(users, name);
            if (newRes instanceof User) {
                user = (User) newRes;
            } else {
                throw new RuntimeException("Got a resource which is not a user. Template: " + template);
            }
        }
        //user.password.setValue( pwd );
        user.setPassword(pwd);
        if (group != null) {
            user.addToGroup(group);
        }
        user.save();
        return user;

    }

    public Group createGroup(String name, String templateName) {
        Folder users = getUsers(true);
        ITemplate template = null;
        if (templateName != null && templateName.length() > 0) {
            template = users.getTemplate(templateName);
        }
        if (template == null) {
            Group group = new Group(users, name);
            group.save();
            return group;
        } else {
            BaseResource newRes = template.createPageFromTemplate(users, name);
            newRes.save();
            if (newRes instanceof Group) {
                return (Group) newRes;
            } else {
                throw new RuntimeException("Got a resource which is not a Group. Template: " + template);
            }
        }
    }

    public void disable() {
        log.debug("disable");
        this.disabled = true;
    }

    public void enable() {
        log.debug("enable");
        this.disabled = false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public int recentHits(String path, String method, int numDays) {
        return _(StatsService.class).queryLastDays(this, path, numDays, method);
    }

    public int activeSubDomains(String method, int numDays) {
        String baseDomain = this.getName().replace("www.", "");
        return _(StatsService.class).activeHosts(baseDomain, method, numDays);
    }

    public List<UnderlayVector> getUnderlayVectors() {
        return underlayVectors;
    }

    public void setUnderlayVectors(List<UnderlayVector> underlayVectors) {
        this.underlayVectors = underlayVectors;
    }

    public Path getAliasedHostPath() {
        return hostPath;
    }

    public void setAliasedHostPath(Path hostPath) {
        this.hostPath = hostPath;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }        
}
