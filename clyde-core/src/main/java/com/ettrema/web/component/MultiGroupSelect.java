package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Request;
import com.ettrema.web.Component;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import com.ettrema.web.User;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;
import com.ettrema.logging.LogUtils;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.web.*;
import com.ettrema.web.groups.ClydeGroupHelper;
import com.ettrema.web.groups.GroupService;
import com.ettrema.web.security.CustomUserGroup;
import com.ettrema.web.security.PermissionChecker;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.UserGroup;
import java.util.List;

/**
 * Used to toggle group membership on and off
 *
 * @author brad
 */
public final class MultiGroupSelect implements WrappableComponent, Component {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiGroupSelect.class);
    private static final long serialVersionUID = 1L;
    private Addressable container;
    private String name;

    public MultiGroupSelect(Addressable container, String name) {
        this.container = container;
        this.name = name;
    }

    public MultiGroupSelect(Addressable container, Element el) {
        this.container = container;
        name = InitUtils.getValue(el, "name");
    }

    @Override
    public void init(Addressable container) {
        if (container == null) {
            throw new IllegalArgumentException("container is null");
        }
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = new Element("component");
        el.addContent(e2);
        String cName = name;
        e2.setAttribute("name", cName);
        e2.setAttribute("class", this.getClass().getName());
        return e2;
    }

    @Override
    public String render(RenderContext rc) {
        return "";
    }

    @Override
    public String renderEdit(RenderContext child) {
        return renderEdit(child.getTargetPage(), child);
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        onPreProcess(rc.getTargetPage(), rc, parameters, files);
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;

    }

    @Override
    public String getName() {
        return name;
    }

    public Path getPath(RenderContext rc) {
        return Path.path(name);
    }

    protected Boolean parse(String s) {
        return Boolean.parseBoolean(s);
    }

    @Override
    public void onPreProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.trace("onPreProcess");
        try {
            GroupService groupService = _(GroupService.class);
            Request request = _(CurrentRequestService.class).request();
            Auth auth = request.getAuthorization();
            PermissionChecker permissionChecker = _(PermissionChecker.class);
            User user = (User) container;
            for (String s : parameters.keySet()) {
                if (s.endsWith("Group")) {
                    String groupName = s.replace("Group", "");
                    UserGroup group = groupService.getGroup(user, groupName);
                    boolean isSelected = parameters.containsKey(s + "Val");
                    LogUtils.trace(log, "onPreProcess: group", groupName, "selected:", isSelected);
                    if (group != null) {
                        if (group instanceof Group) {
                            Group g = (Group) group;
                            if (isSelected) {
                                // this parameters presence indicates that the group is selected                                
                                if( !user.isInGroup(g)) {
                                    if( userCanAddToGroup(g, permissionChecker, auth)) {
                                        log.trace(" - add to group");
                                        user.addToGroup(g);
                                    } else {
                                        setValidationMessage("You can't add to this group: " + g.getName());
                                        break;
                                    }
                                }
                            } else {
                                if( user.isInGroup(g)) {
                                    log.trace(" - remove from group");
                                    user.removeFromGroup(g);
                                }
                            }
                        } else {
                            log.warn("UserGroup is not instanceof Group: " + group.getClass());
                        }
                    } else {
                        log.warn("group not found: " + groupName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("group exception", e);
            rc.addAttribute("validation" + getPath(rc), e.getMessage());
        }

    }

    @Override
    public String onProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;

    }

    @Override
    public String render(Addressable container, RenderContext rc) {
        return "";
    }

    @Override
    public String renderEdit(Addressable container, RenderContext child) {        
        User user = (User) child.page;
        List<? extends CustomUserGroup> availableGroups = _(ClydeGroupHelper.class).getAvailableGroups(child.page.getHost());
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for( CustomUserGroup g : availableGroups ) {
            if( g instanceof Group ) {
                Group group = (Group) g;
                String checked = user.isInGroup(group) ? "checked='yes'" : "";
                LogUtils.trace(log, "renderEdit: group", group.getName(), "checked?", checked);
                String groupName = group.getName();
                String label = "<label for='" + groupName + "Check'>" + groupName + "</label>";
                String dummy = "<input type='hidden' name='" + groupName + "Group' value='1'/>";
                String selection = "<input id='" + groupName + "Check' type='checkbox' name='" + groupName + "GroupVal' value='true' " + checked + " />" + dummy;
                sb.append("<li class='groupSelect'>").append(selection).append(dummy).append(label).append("</li>");
            }
        }
        sb.append("</ul>");        
        return sb.toString();
    }

    @Override
    public boolean validate(Addressable container, RenderContext rc) {
        String attName = "validation" + getPath(rc);
        String validationMessage = (String) rc.getAttribute(attName);
        if (validationMessage != null) {
            log.warn("validation error: " + validationMessage);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<Group> getValue(Addressable container) {
        User user = (User) container;
        return _(ClydeGroupHelper.class).getGroups(user);
    }

    @Override
    public String getFormattedValue(Addressable container) {
        return getValue(container) + "";
    }

    @Override
    public boolean validate(RenderContext rc) {
        return validate(rc.page, rc);
    }

    public final void setValidationMessage(String s) {
        RequestParams params = RequestParams.current();
        params.attributes.put(this.getName() + "_validation", s);
    }

    @Override
    public final String getValidationMessage() {
        RequestParams params = RequestParams.current();
        return (String) params.attributes.get(this.getName() + "_validation");
    }

    private boolean userCanAddToGroup(Group g, PermissionChecker permissionChecker, Auth auth) {
        return permissionChecker.hasRole(Role.AUTHOR, g, auth);
    }
}
