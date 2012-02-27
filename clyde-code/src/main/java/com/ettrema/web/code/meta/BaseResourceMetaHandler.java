package com.ettrema.web.code.meta;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.DateUtils.DateParseException;
import com.ettrema.web.eval.EvalUtils;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.User;
import com.ettrema.web.security.Subject;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.BaseResource.RoleAndGroup;
import com.ettrema.web.IUser;
import com.ettrema.web.Templatable;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.comments.Comment;
import com.ettrema.web.comments.CommentService;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.groups.GroupService;
import com.ettrema.web.security.Permission;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.Permissions;
import com.ettrema.web.security.UserGroup;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class BaseResourceMetaHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BaseResourceMetaHandler.class);
    private final CommonTemplatedMetaHandler commonTemplatedMetaHandler;
    private final GroupService groupService;
    private final CommentService commentService;

    public BaseResourceMetaHandler(CommonTemplatedMetaHandler commonTemplatedMetaHandler, GroupService groupService, CommentService commentService) {
        this.commonTemplatedMetaHandler = commonTemplatedMetaHandler;
        this.groupService = groupService;
        this.commentService = commentService;
    }

    public void populateXml(Element e2, BaseResource res) {
        // do not include content fields (title,body) because they will be in the content file
        populateXml(e2, res, false);
    }

    public void populateXml(Element e2, BaseResource res, boolean includeContentValues) {
        log.trace("populateXml");
        InitUtils.setString(e2, "redirect", res.getRedirect());
        Element elPerms = null;
        elPerms = populateGroupPermissions(res, elPerms, e2);
        populatePermissions(res, elPerms, e2);

        Element elRoleRules = new Element("roleRules", CodeMeta.NS);
        e2.addContent(elRoleRules);
        EvalUtils.setEvalDirect(elRoleRules, res.getRoleRules(), CodeMeta.NS);

        commonTemplatedMetaHandler.populateXml(e2, res, includeContentValues);
        populateComments(e2, res);
    }

    private Element populateGroupPermissions(BaseResource res, Element elPerms, Element e2) {
        List<RoleAndGroup> groupPermissions = res.getGroupPermissions();
        if (groupPermissions != null && !groupPermissions.isEmpty()) {
            log.trace("add groups");
            for (RoleAndGroup rag : res.getGroupPermissions()) {
                Element elRag = new Element("groupPerm", CodeMeta.NS);
                if (elPerms == null) {
                    elPerms = new Element("permissions", CodeMeta.NS);
                    e2.addContent(elPerms);
                }
                elPerms.addContent(elRag);
                elRag.setAttribute("group", rag.getGroupName());
                elRag.setAttribute("role", rag.getRole().name());
            }
        }
        return elPerms;
    }

    private void populatePermissions(BaseResource res, Element elPerms, Element e2) {
        Permissions perms = res.permissions();
        if (perms != null) {
            for (Permission perm : perms) {
                if (elPerms == null) {
                    elPerms = new Element("permissions", CodeMeta.NS);
                    e2.addContent(elPerms);
                }

                Element elRag;
                Subject grantee = perm.getGrantee();
                if (grantee instanceof User) {
                    elRag = new Element("userPerm", CodeMeta.NS);
                    User granteeUser = (User) grantee;
                    elRag.setAttribute("path", granteeUser.getUrl());
                } else if (grantee instanceof UserGroup) {
                    UserGroup granteeGroup = (UserGroup) grantee;
                    elRag = new Element("groupPerm", CodeMeta.NS);
                    elRag.setAttribute("group", granteeGroup.getSubjectName());
                } else {
                    log.debug("unsupported permission recipient type: " + grantee.getClass());
                    elRag = null;
                }
                if (elRag != null) {
                    elPerms.addContent(elRag);
                    elRag.setAttribute("role", perm.getRole().toString());
                }
            }
        }
    }

    public void applyOverrideFromXml(BaseResource r, Element d) {
        commonTemplatedMetaHandler.applyOverrideFromXml(r, d);
    }

    void updateFromXml(BaseResource res, Element el) {
        updateFromXml(res, el, false);
    }

    void updateFromXml(BaseResource res, Element el, boolean includeContentVals) {
        commonTemplatedMetaHandler.updateFromXml(res, el, includeContentVals);

        res.setRedirect(InitUtils.getValue(el, "redirect"));

        Element elRoleRules = el.getChild("roleRules", CodeMeta.NS);
        if (elRoleRules != null) {
            res.setRoleRules(EvalUtils.getEvalDirect(elRoleRules, CodeMeta.NS, res));
        }
        updatePermissions(el, res);
        updateComments(el, res);
    }

    private void updatePermissions(Element el, BaseResource res) throws RuntimeException {
        // Only update permissions if permissions have been specified
        Element elPermissions = el.getChild("permissions", CodeMeta.NS);
        if (elPermissions != null) {
            Permissions previousPerms = res.permissions();
            if (previousPerms != null) {
                log.trace("remove all previous permissions");
                for (Permission perm : previousPerms) {
                    log.trace("remove: " + perm);
                    previousPerms.revoke(perm.getRole(), perm.getGrantee());
                }
            }

            List<Element> permElements = JDomUtils.childrenOf(el, "permissions", CodeMeta.NS);
            if (permElements.size() > 0) {
                log.trace("adding permissions: " + permElements.size());
                for (Element elPerm : permElements) {
                    String roleName = elPerm.getAttributeValue("role");
                    Role role;
                    if (!StringUtils.isEmpty(roleName)) {
                        roleName = roleName.trim();
                        try {
                            role = Role.valueOf(roleName);
                        } catch (Exception e) {
                            log.error("unknown role: " + roleName, e);
                            throw new RuntimeException("Unknown role: " + roleName);
                        }
                        String type = elPerm.getName();
                        if (type.equals("groupPerm")) {
                            String groupName = elPerm.getAttributeValue("group");
                            if (StringUtils.isEmpty(groupName)) {
                                throw new RuntimeException("Group attribute is empty");
                            }
                            UserGroup group = groupService.getGroup(res, groupName);
                            if (group != null) {
                                log.info("Add new permission: " + role + " - " + group.getSubjectName());
                                res.permissions(true).grant(role, group);
                            } else {
                                log.error("Group not found: " + groupName);
                            }
                        } else if (type.equals("userPerm")) {
                            String userPath = elPerm.getAttributeValue("path");
                            Resource r = res.getHost().find(userPath);
                            if (r == null) {
                                throw new RuntimeException("User path not found: " + userPath + " in host: " + res.getHost().getName());
                            } else if (r instanceof User) {
                                User u = (User) r;
                                res.permissions(true).grant(role, u);
                            }
                        } else {
                            throw new RuntimeException("Unknown permission type: " + type);
                        }
                    } else {
                        throw new RuntimeException("empty role name");
                    }
                }
            }
        }
    }

    private void updateComments(Element el, BaseResource res) {
        Element elComments = el.getChild("comments", CodeMeta.NS);
        if (elComments == null) {
            return;
        }
        commentService.deleteAll(res.getNameNode());
        for (Element elComment : JDomUtils.children(el)) {
            String commentBody = JDomUtils.getInnerXml(elComment);
            Date date;
            try {
                date = InitUtils.getDate(el, "date");
            } catch (DateParseException ex) {
                log.error("exception parsing date for comment in resource: " + res.getHref(), ex);
                date = new Date();
            }
            Path userPath = InitUtils.getPath(el, "user");
            IUser commentUser = null;
            if (userPath != null) {
                Templatable ouser = res.find(userPath);
                if (ouser instanceof IUser) {
                    commentUser = (IUser) ouser;
                } else {
                    log.error("Couldnt locate user: " + userPath);
                }
            }
            try {
                commentService.newComment(res.getNameNode(), commentBody, date, commentUser);
            } catch (NotAuthorizedException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    private void populateComments(Element e2, BaseResource res) {
        List<Comment> comments = commentService.comments(res.getNameNode());
        if( comments == null || comments.isEmpty()) {
            return ;
        }
        Element elComments = new Element("comments", CodeMeta.NS);
        e2.addContent(elComments);
        for(Comment c : comments) {
            Element el = new Element("comment", CodeMeta.NS);
            elComments.addContent(el);
            el.setText(c.getComment());
            String userPath = c.getUser().getHref();
            el.setAttribute("user", userPath);
            InitUtils.set(el, "date", c.getDate());
        }
    }
}
