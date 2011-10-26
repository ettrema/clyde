package com.ettrema.web.security;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.EmailAddress;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class UserLocator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserLocator.class);
    
    public List<User> search(String name, CommonTemplated currentResource) {        
        Set<User> foundUsers = new HashSet<User>();
        MailboxAddress add = parse(name);
        if( add != null ) {
            User user = findUserByEmail(add, currentResource);
            if( user != null ) {
                foundUsers.add(user);
            }
        }
        List<User> list = findUserByName(name, currentResource);
        for( User u : list ) {
            foundUsers.add(u);
        }
        List<User> users = new ArrayList<User>(foundUsers);
        Collections.sort(users);
        return users;        
    }
    
    
    public User findUser(Host host, String name) {
        Folder users = host.getUsers();
        if (users == null) {
            return null;
        }
        Resource res = users.child(name.trim());
        if (res != null) {
            if (res instanceof User) {
                return (User) res;
            } else {
                log.warn("found an instance, but not a user: " + res.getName());
            }
        }
        
        return null;
    }    
    
    /**
     * Given a starting (ie current) resource and an email address, attempt to
     * locate a matching user. The user account must be defined on the same domain
     * as the given resource
     *
     * @param add
     * @param currentResource
     * @return
     */
    public User findUserByEmail(MailboxAddress add, CommonTemplated currentResource) {
        List<User> foundUsers = findMatchingUsers(add);
        Host h = currentResource.getHost();
        for (User user : foundUsers) {
            if (isMatchingDomain(user, h)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Searches for a user with a matching user name anywhere in the current host
     * (ie not just the user's folder)
     * 
     * @param add
     * @param currentResource
     * @return 
     */
    public List<User> findUserByName(String name, CommonTemplated currentResource) {
        List<User> foundUsers = findMatchingUsers(name);
        Iterator<User> it = foundUsers.iterator();
        Host h = currentResource.getHost();
        while(it.hasNext()) {
            User user = it.next();
            if (!isMatchingDomain(user, h)) {
                it.remove();
            }
        }
        return foundUsers;
    }
    

    private List<User> findMatchingUsers(MailboxAddress add) {
        VfsSession vfs = RequestContext.getCurrent().get(VfsSession.class);
        List<NameNode> list = vfs.find(EmailAddress.class, add.toPlainAddress());
        List<User> foundUsers = new ArrayList<User>();
        if (list == null || list.isEmpty()) {
            log.debug("no nodes found");
        } else {
            for (NameNode node : list) {
                NameNode nUser = node.getParent().getParent(); // the first parent is just a holder
                DataNode dnUser = nUser.getData();
                if (dnUser != null && dnUser instanceof User) {
                    User user = (User) dnUser;
                    foundUsers.add(user);
                } else {
                    log.warn("parent is not a user: " + dnUser.getClass());
                }
            }
        }
        return foundUsers;
    }
    
    private List<User> findMatchingUsers(String name) {
        VfsSession vfs = RequestContext.getCurrent().get(VfsSession.class);
        List<NameNode> list = vfs.find(User.class, name);
        List<User> foundUsers = new ArrayList<User>();
        if (list == null || list.isEmpty()) {
            log.debug("no nodes found");
        } else {
            for (NameNode node : list) {
                DataNode dnUser = node.getData();
                if (dnUser != null && dnUser instanceof User) {
                    User user = (User) dnUser;
                    foundUsers.add(user);
                } else {
                    log.warn("not a user: " + dnUser.getClass());
                }
            }
        }
        return foundUsers;
    }    

    private boolean isMatchingDomain(User user, Host h) {
        if (h == null) {
            return false;
        }
        if (user.getHost().getNameNodeId().equals(h.getNameNodeId())) {
            return true;
        }
        return isMatchingDomain(user, h.getParentHost());
    }    
    
    public MailboxAddress parse(String user) {
        if (user.contains("@")) {
            try {
                MailboxAddress add = MailboxAddress.parse(user.trim());
                return add;
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            return null;
        }
    }    
}
