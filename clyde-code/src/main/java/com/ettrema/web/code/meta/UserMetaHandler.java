package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import static com.ettrema.context.RequestContext._;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.Folder;
import com.ettrema.web.Group;
import com.ettrema.web.User;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.groups.RelationalGroupHelper;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class UserMetaHandler implements MetaHandler<User> {

    public static final String ALIAS = "user";
    private final FolderMetaHandler folderMetaHandler;

    public UserMetaHandler(FolderMetaHandler folderMetaHandler) {
        this.folderMetaHandler = folderMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return User.class;
    }

    @Override
    public boolean supports(Resource r) {
        return r instanceof User;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public Element toXml(User r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    @Override
    public User createFromXml(CollectionResource parent, Element d, String name) {
        User page = new User((Folder) parent, name);
        updateFromXml(page, d);
        return page;
    }

    private void populateXml(Element el, User user) {
        InitUtils.setBoolean(el, "emailDisabled", user.isEmailDisabled());

        Element elEmail = new Element("email", CodeMeta.NS);
        elEmail.setText(user.getExternalEmailText());
        el.addContent(elEmail);
        
        Element elProfilePic = new Element("profilePicHref", CodeMeta.NS);
        elEmail.setText(user.getProfilePicHref());
        el.addContent(elProfilePic);
        
        populateGroups(el, user);

        folderMetaHandler.populateXml(el, user);
    }

    @Override
    public void updateFromXml(User user, Element el) {
        user.setEmailDisabled(InitUtils.getBoolean(el, "emailDisabled"));
        String newEmail = JDomUtils.valueOf(el, "email", CodeMeta.NS);
        user.setExternalEmailText(newEmail);
        String profilePicHref = JDomUtils.valueOf(el, "profilePicHref", CodeMeta.NS);
        user.setProfilePicHref(profilePicHref);
        updateGroups(el, user);
        user.save();
        folderMetaHandler.updateFromXml(user, el);
    }

    @Override
    public void applyOverrideFromXml(User r, Element el) {
        folderMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }
    
    

    private void populateGroups(Element el, User user) {
        RelationalGroupHelper groupService = _(RelationalGroupHelper.class);
        List<Group> groups = groupService.getGroups(user);
        Element elGroups = new Element("groups", CodeMeta.NS);
        el.addContent(elGroups);
        for (Group g : groups) {
            Element elGroup = new Element("group", CodeMeta.NS);
            elGroup.setAttribute("name", g.getName());
            elGroups.addContent(elGroup);
        }
    }

    private void updateGroups(Element el, User user) {
        RelationalGroupHelper groupService = _(RelationalGroupHelper.class);
        List<Element> groupElements = JDomUtils.childrenOf(el, "groups", CodeMeta.NS);
        groupService.removeFromAllGroups(user);
        if (groupElements == null || groupElements.isEmpty()) {
            // not a member of any groups            
        } else {
            for (Element elGroup : groupElements) {
                String groupName = elGroup.getAttributeValue("name");
                user.addToGroup(groupName);
            }
        }
    }
}
