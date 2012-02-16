package com.ettrema.web.groups;

import com.ettrema.web.Host;
import com.bradmcevoy.http.Auth;
import com.ettrema.web.security.PermissionChecker;
import com.bradmcevoy.http.Resource;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.utils.LogUtils;
import com.ettrema.utils.RelationUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Group;
import com.ettrema.web.IUser;
import com.ettrema.web.Templatable;
import com.ettrema.web.security.CustomUserGroup;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.Subject;
import com.ettrema.web.security.UserGroup;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.vfs.Relationship;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

import static com.ettrema.context.RequestContext._;
import com.ettrema.web.*;
import java.util.Collections;

/**
 * Implements persistence of group information for clyde resources
 *
 *
 * @author brad
 */
public class RelationalGroupHelper implements GroupService, ClydeGroupHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RelationalGroupHelper.class);
    public static final String REL_NAME = "memberOf";
    private final GroupService wrapped = new SystemGroupService();
    private final PermissionChecker permissionChecker;

    public RelationalGroupHelper(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    /**
     * Get a list of groups available for selection for a user. Includes
     * groups which might require particular permissions, even if the current
     * user does not have permissions to add to those groups.
     * 
     * @param web
     * @return 
     */
    @Override
    public List<? extends CustomUserGroup> getAvailableGroups(Host host) {
        LogUtils.trace(log, "getAvailableGroups", host.getName());
        return host.getGroups();
    }
    
    @Override
    public List<Subject> getMembers(UserGroup group) {
        log.trace("getMembers");
        NameNode nFrom;
        if (group instanceof Group) {
            Group clydeGroup = (Group) group;
            List<Subject> list = new ArrayList<>();
            List<Relationship> rels = clydeGroup.getNameNode().findToRelations(REL_NAME);
            if (rels != null && rels.size() > 0) {
                for (Relationship r : rels) {
                    nFrom = r.from();
                    if (nFrom != null && nFrom.getData() instanceof Subject) {
                        Subject subject = (Subject) nFrom.getData();
                        list.add(subject);
                    }
                }
            }
            return list;
        } else {
            return null;
        }
    }

    @Override
    public void addToGroup(IUser user, CustomUserGroup group) {
        log.trace("addToGroup");
        if (group == null) {
            throw new NullPointerException("group is null");
        }
        if (group instanceof Group) {
            Group clydeGroup = (Group) group;
            if (clydeGroup.isSecure()) {
                Auth auth = _(CurrentRequestService.class).request().getAuthorization();

                if (!permissionChecker.hasRole(Role.AUTHOR, clydeGroup, auth)) {
                    throw new RuntimeException("The current user does not have permission to add users to this group. You must be an Author of the group");
                }
            }
            if (group.isInGroup(user)) {
                log.trace("user is already in group");
                return;
            }
            Relationship r = user.getNameNode().makeRelation(clydeGroup.getNameNode(), REL_NAME);
            if (log.isTraceEnabled()) {
                log.trace("created relation from: " + r.from().getId() + " -> " + r.to().getId());
            }
            clydeGroup.getNameNode().onNewRelationship(r);
        } else {
            throw new RuntimeException("Cant add to: " + group.getClass());
        }
    }

    @Override
    public UserGroup getGroup(Resource relativeTo, String name) {
        if (log.isTraceEnabled()) {
            log.trace("getGroup: " + name);
        }
        if (relativeTo instanceof Templatable) {
            Templatable t = (Templatable) relativeTo;
            Host h = t.getHost();
            if (log.isTraceEnabled()) {
                log.trace("look for group in host: " + h.getName());
            }
            Group g = h.findGroup(name);
            if (g != null) {
                log.trace("found group");
                return g;
            }
        }
        log.trace("couldnt get persisted group, so look for system group");
        return wrapped.getGroup(relativeTo, name);
    }

    @Override
    public boolean isInGroup(IUser user, Group group) {
        LogUtils.trace(log, "isInGroup: user: ", user.getName(), "group:", group.getName());
        List<Relationship> rels = user.getNameNode().findFromRelations(REL_NAME);
        if (CollectionUtils.isEmpty(rels)) {
            return false;
        } else {
            for (Relationship r : rels) {
                BaseResource res = RelationUtils.to(r);
                if (res instanceof Group) {
                    Group g = (Group) res;
                    if (g.getNameNodeId().equals(group.getNameNodeId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public List<Group> getGroups(IUser user) {
        LogUtils.trace(log, "getGroups: user: ", user.getName());
        List<Relationship> rels = user.getNameNode().findFromRelations(REL_NAME);
        if (CollectionUtils.isEmpty(rels)) {
            return Collections.EMPTY_LIST;
        } else {
            List<Group> list = new ArrayList<>();
            for (Relationship r : rels) {
                BaseResource res = RelationUtils.to(r);
                if (res instanceof Group) {
                    Group g = (Group) res;
                    list.add(g);
                }
            }
            return list;
        }
    }    

    public void removeFromGroup(IUser user, Group group) {
        log.trace("removeFromGroup");
        List<Relationship> rels = user.getNameNode().findFromRelations(REL_NAME);
        if (CollectionUtils.isEmpty(rels)) {
            return;
        } else {
            for (Relationship r : rels) {
                BaseResource res = RelationUtils.to(r);
                if (res instanceof Group) {
                    Group g = (Group) res;
                    if (g.getNameNodeId().equals(group.getNameNodeId())) {
                        user.getNameNode().onDeletedFromRelationship(r);
                        RelationalNameNode groupNode = (RelationalNameNode) r.to();
                        groupNode.onDeletedToRelationship(r);
                        r.delete();
                    }
                }
            }
        }
    }

    public void removeFromAllGroups(IUser user) {
        LogUtils.trace(log, "removeFromAllGroups: user: ", user.getName());
        List<Relationship> rels = user.getNameNode().findFromRelations(REL_NAME);
        if (!CollectionUtils.isEmpty(rels)) {
            for (Relationship r : rels) {
                BaseResource res = RelationUtils.to(r);
                if (res instanceof Group) {
                    Group g = (Group) res;
                    r.delete();
                }
            }
        }        
    }
}
