package com.bradmcevoy.web.security;

import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import com.bradmcevoy.web.BaseResource.RoleAndGroup;
import com.bradmcevoy.web.groups.GroupService;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

import static com.ettrema.context.RequestContext._;

/**
 * Implementation of PermissionChecker which works for Clyde resources
 * 
 * Uses the permissions() method on BaseResource to check for permissions
 *
 * @author brad
 */
public class ClydePermissionChecker implements PermissionChecker {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydePermissionChecker.class);
    private final boolean allowAnonymous;

    /**
     *
     * @param allowAnonymous - true to allow anonymous access when the ANONYMOUS role
     * is required
     */
    public ClydePermissionChecker(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public ClydePermissionChecker() {
        allowAnonymous = true;
    }

    @Override
    public boolean hasRole(Role role, Resource r, Auth auth) {
        log.debug("hasRole: " + role);
        if (allowAnonymous && role.equals(Role.ANONYMOUS)) {
            return true;
        }
        if (role.equals(Role.AUTHENTICATED)) {
            return auth != null && auth.getTag() != null;
        } else if (role.equals(Role.CREATOR)) {
            IUser iuser = _(CurrentUserService.class).getSecurityContextUser();
            if (iuser == null) {
                log.trace("no current user so cannot be creator");
                return false;
            } else if (!(iuser instanceof User)) {
                log.trace("incompatible user type for CREATOR role");
                return false;
            } else if (!(r instanceof BaseResource)) {
                log.trace("resource is not compatible for CREATOR role");
                return false;
            } else {
                BaseResource res = (BaseResource) r;
                User creator = res.getCreator();
                if (creator == null) {
                    log.trace("resource has no creator");
                    return false;
                } else {
                    if (!creator.is(iuser)) {
                        log.trace("current user is not creator");
                        return false;
                    } else {
                        log.trace("current user is creator");
                        return true;
                    }
                }
            }
        } else if (r instanceof BaseResource) {
            BaseResource res = (BaseResource) r;
            IUser iuser = _(CurrentUserService.class).getSecurityContextUser();
            if (iuser == null) {
                log.trace("no current user so deny access");
                return false;
            } else if (!(iuser instanceof User)) {
                log.trace("incompatible user type");
                return false;
            }
            return hasRoleDirect((User) iuser, res, role);
        } else if (r instanceof Templatable) {
            Templatable templatable = (Templatable) r;
            boolean b = hasRole(role, templatable.getParent(), auth);
            if (!b) {
                log.warn("user does not have role: " + role + " on resource: " + templatable.getHref());
            }
            return b;
        } else {
            log.warn("ClydePermissionChecker cannot check permission on resource of type: " + r.getClass() + " Saying no to be safe");
            return false;
        }
    }

    private boolean hasRoleDirect(User user, BaseResource res, Role role) {
        Permissions ps = res.permissions();
        if (ps != null) {
            if (ps.allows(user, role)) {
                log.trace("hasRoleRes: - found acceptable permision");
                return true;
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("No explicite permissions granted to user, check groups on: " + res.getHref());
        }
        if (res != null) {
            GroupService gs = _(GroupService.class);
            for (RoleAndGroup rag : res.getGroupPermissions()) {
                if (rag.getRole() == role) {
                    UserGroup group = gs.getGroup(res, rag.getGroupName());
                    if (group != null) {
                        log.trace("allows: found group with role");
                        if (group.isInGroup(user)) {
                            log.trace("allows: user is in group");
                            return true;
                        }
                    }
                }
            }
        }
        Boolean bRuleResult = checkRules(res, user, role);
        if (bRuleResult != null) {
            if (log.isTraceEnabled()) {
                log.trace("allows: result from rules: " + bRuleResult);
            }
            return bRuleResult.booleanValue();
        }

        //Nothing set directly or in rules, so look to parent
        return hasRoleRecursive(user, res.getParent(), role);
    }

    /**
     * Don't evaluate rules on parent resources
     * 
     * @param user
     * @param res
     * @param role
     * @return 
     */
    private boolean hasRoleRecursive(User user, BaseResource res, Role role) {
        if (res == null) {
            log.trace("resource is null");
            return false;
        }

        if (log.isTraceEnabled()) {
            log.trace("hasRoleRes: check for role: " + role + " on resource: " + res.getHref());
        }
        Permissions ps = res.permissions();
        if (ps != null) {
            if (ps.allows(user, role)) {
                log.trace("hasRoleRes: - found acceptable permision");
                return true;
            }
        }

        GroupService gs = _(GroupService.class);        
        for (RoleAndGroup rag : res.getGroupPermissions()) {
            if (rag.getRole() == role) {
                UserGroup group = gs.getGroup(res, rag.getGroupName());
                if (group != null) {
                    log.trace("allows: found group with role");
                    if (group.isInGroup(user)) {
                        log.trace("allows: user is in group");
                        return true;
                    }
                }
            }
        }

        if (res instanceof Host) {
            log.trace("reached host, no permissions found");
            return false;
        } else {
            return hasRoleRecursive(user, res.getParent(), role);
        }
    }

    private Boolean checkRules(BaseResource res, Subject user, Role role) {
        log.trace("checkRules");
        Evaluatable rules = res.getRoleRules();
        ITemplate t = res.getTemplate();
        if (rules != null) {
            RenderContext rc = new RenderContext(t, res, null, false);
            Object r = EvalUtils.eval(rules, rc, res);
            Boolean result = Formatter.getInstance().toBool(r);
            return result;
        } else {
            while (t != null) {
                Boolean b = t.hasRole(user, role, res);
                if (b != null) {
                    return b;
                }
                t = t.getTemplate();
            }
            return null;
        }
    }
}
