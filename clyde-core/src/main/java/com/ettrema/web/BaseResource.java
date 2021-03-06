package com.ettrema.web;

import java.util.Collection;
import com.ettrema.web.security.UserLocator;
import com.bradmcevoy.http.Request;
import com.ettrema.web.security.PermissionChecker;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.values.HrefList;
import com.ettrema.web.security.CurrentUserService;
import com.ettrema.web.security.Permission;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import com.ettrema.http.acl.Principal;
import java.util.Set;
import com.ettrema.event.LogicalDeleteEvent;
import com.ettrema.event.PhysicalDeleteEvent;
import com.ettrema.event.PostSaveEvent;
import com.ettrema.event.PreSaveEvent;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.ettrema.event.EventManager;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.utils.AuthoringPermissionService;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.utils.Redirectable;
import com.bradmcevoy.utils.ReflectionUtils;
import com.ettrema.event.ClydeMoveEvent;
import com.ettrema.web.comments.Comment;
import com.ettrema.web.comments.CommentService;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.DateDef;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.NameInput;
import com.ettrema.web.component.TemplateSelect;
import com.ettrema.web.component.Text;
import com.ettrema.web.creation.CreatorService;
import com.ettrema.web.eval.EvalUtils;
import com.ettrema.web.eval.Evaluatable;
import com.ettrema.web.groups.GroupService;
import com.ettrema.web.locking.ClydeLockManager;
import com.ettrema.web.security.BeanProperty;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.Permissions;
import com.ettrema.web.security.UserGroup;
import com.ettrema.event.ResourceEvent;
import com.ettrema.forms.FormAction;
import com.ettrema.forms.FormParameter;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.EmptyDataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.vfs.Relationship;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.Namespace;

import static com.ettrema.context.RequestContext.*;
import com.ettrema.logging.LogUtils;

/**
 * Base class for all physical resources. Encapsulates a namenode and is a
 * datanode
 *
 *
 * @author brad
 */
@BeanPropertyResource(value = "clyde")
public abstract class BaseResource extends CommonTemplated implements DataNode, Addressable, XmlPersistableResource, LockableResource, Redirectable, AccessControlledResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BaseResource.class);
    private static final long serialVersionUID = 1L;
    public static final Namespace NS = Namespace.getNamespace("c", "http://clyde.ettrema.com/ns/core");
    private UUID id;
    protected NameInput nameInput;
    private String redirect;
    private UUID creatorNameNodeId;
    private Date timestamp;
    private Evaluatable roleRules;
    private List<RoleAndGroup> groupPermissions;
    private Date sourceModDate; // modified date of the source from which this file was created. Used for sync
    private List<WebResource> webResources;
    protected transient RelationalNameNode nameNode;
    private transient User creator;
    private transient boolean isNew;
    private transient boolean isSaved;
    /**
     * Just a transient map variable holding logic to access relations through
     * the map syntax
     */
    private transient Relations relations;

    protected abstract BaseResource newInstance(Folder parent, String newName);

    /**
     * If this should be indexed for searching
     *
     * @return
     */
    public abstract boolean isIndexable();

    public static BaseResource importResource(BaseResource parent, Element el, String filename) {
        String className = el.getAttributeValue("class");
        if (className == null || className.length() == 0) {
            throw new IllegalArgumentException("class is empty: " + filename);
        }
        BaseResource res = (BaseResource) ReflectionUtils.create(className, parent, filename);
        res.loadFromXml(el);
        return res;
    }

    /**
     * For root only
     */
    protected BaseResource() {
        super();
    }

    /**
     * Usual constructor;
     */
    public BaseResource(String contentType, Folder parentFolder, String newName) {
        if (newName.contains("/")) {
            throw new IllegalArgumentException("Names cannot contain forward slashes: " + newName);
        }
        isNew = true;
        if (parentFolder != null) {
            this.nameNode = (RelationalNameNode) parentFolder.onChildCreated(newName, this);
        } else {
            log.warn("no parent folder provided");
        }
        setContentType(contentType);
        initName();

    }

    /**
     * A true value indicates that this instance has been created within this
     * transaction. Note that the object may have been saved, as it can be saved
     * multiple times within a transaction.
     *
     * See isSaved to determine whether the record has been saved
     *
     * @return
     */
    public boolean isNew() {
        return isNew;
//        log.warn( "isNew: " + nameNode.isNew() + " - "+ nameNode.getClass() );
//        return nameNode.isNew();
    }

    public boolean isSaved() {
        return isSaved;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    final protected void initName() {
        ComponentMap map = this.getComponents();
        if (!map.containsKey("name")) {
            nameInput = new NameInput(this);
            map.add(nameInput);    // everyone has a name component        
        }
    }

    public boolean hasChildNode(String name) {
        NameNode child = this.nameNode.child(name);
        return child != null;
    }

    public NameNode addChildNode(String name, DataNode dn) {
        return this.nameNode.add(name, dn);
    }

    public void removeChildNode(String name) {
        NameNode child = this.nameNode.child(name);
        if (child == null) {
            return;
        }
        child.delete();
    }

    public RelationalNameNode getNameNode() {
        return nameNode;
    }

    /**
     * The ID of the name node which contains this resource
     *
     * @return
     */
    public UUID getNameNodeId() {
        if (nameNode == null) {
            return null;
        }
        return nameNode.getId();
    }

    /**
     * For a physical resource, getParentFolder returns exactly the same result
     * as getParent
     *
     * @return
     */
    @Override
    public Folder getParentFolder() {
        return getParent();
    }

    /**
     * Move this resource to the given folder (aka collection) with the given
     * name
     *
     * The name may be unchanged for a conventional move, or the collection
     * might be unchanged for a conventional rename operation, but both can be
     * changed simultaneously
     *
     * This method commits the transaction
     *
     * @param rDest
     * @param name
     */
    public void moveTo(CollectionResource rDest, String name) throws NotAuthorizedException, ConflictException, BadRequestException {
        moveTo(rDest, name, true);
    }

    public void moveTo(CollectionResource rDest, String name, boolean commit) throws NotAuthorizedException, ConflictException, BadRequestException {
        if (rDest instanceof Folder) {
            Folder fDest = (Folder) rDest;
            IUser user = _(CurrentUserService.class).getSecurityContextUser();
            if (user != null) {
                if (!fDest.getName().equals("Trash")) {
                    Role neededRole = _(AuthoringPermissionService.class).getCreateRole(fDest, getTemplate());
                    Request req = _(CurrentRequestService.class).request();
                    boolean isOk = _(PermissionChecker.class).hasRole(neededRole, fDest, req.getAuthorization());
                    if (log.isTraceEnabled()) {
                        log.trace("check move destination security. does current user have role: " + neededRole + " = " + isOk);
                    }
                    if (!isOk) {
                        throw new NotAuthorizedException(rDest);
                    }
                } else {
                    log.trace("skip dest security check because its the trash folder");
                }
            } else {
                log.trace("No current user, so skip move destination security check");
            }
            _moveTo(fDest, name);
        } else {
            throw new RuntimeException("destination collection is not a known type. Is a: " + rDest.getClass().getName());
        }
        if (commit) {
            commit();
        }
    }

    public void _moveTo(Folder fDest, String name) throws ConflictException, BadRequestException, NotAuthorizedException {
        log.debug("moveTo: name: " + name);
        Folder oldParent = getParent();
        this.nameNode.move(fDest.getNameNode(), name);
//        if( !fDest.getPath().equals(getParent().getPath()) ) {
//            log.debug("..moving folder to: " + fDest.getHref());
//            this.nameNode.move(fDest.nameNode, name);
//        }
        save();
        _(EventManager.class).fireEvent(new ClydeMoveEvent(this, oldParent));
    }

    /**
     * Copy to the given folder (ie collection) with the given name
     *
     * This method commits the transaction
     *
     * @param toCollection
     * @param name
     */
    public void copyTo(CollectionResource toCollection, String name) {
        log.debug("copyTo: from " + this.getName() + " to " + toCollection.getName() + ":" + name);
        if (toCollection instanceof Folder) {
            Folder newParent = (Folder) toCollection;
            _copyTo(newParent, name);
            commit();
        } else {
            throw new IllegalArgumentException("toCollection is not of type folder. Is: " + toCollection.getClass());
        }
    }

    /**
     * Performs a copy to the given folder with the current name
     *
     * does not commit
     *
     * @param newParent
     */
    public void _copyTo(Folder newParent) {
        _copyTo(newParent, this.getName());
    }

    public void _copyTo(Folder newParent, String name) {
        BaseResource newRes = copyInstance(newParent, name);
        newRes.templateSelect = (TemplateSelect) newRes.componentMap.get("template");
        newRes.save();
        log.debug("created: " + newRes.getName() + " - " + newRes.getClass());
    }

    protected BaseResource copyInstance(Folder parent, String newName) {
        BaseResource newRes = newInstance(parent, newName);
        newRes.setContentType(this.getContentType(null));
        newRes.valueMap.addAll(this.valueMap);
        newRes.componentMap.addAll(this.componentMap);
        String email = this.getExternalEmailTextV2("default");
        newRes.setExternalEmailTextV2("default", email);
        return newRes;
    }

    /**
     * Delete the resource and commit
     *
     */
    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        deleteNoTx();
        commit();
    }

    /**
     * Delete without committing the transaction
     *
     */
    public void deleteNoTx() throws NotAuthorizedException, ConflictException, BadRequestException {
        log.debug("delete: " + this.getName());
        _delete();
    }

    /**
     * called from delete(). moves to trash, unless its already in trash or is a
     * host
     *
     * we don't put Host's in trash because they define their own Trash folder
     */
    public void _delete() throws ConflictException, BadRequestException, NotAuthorizedException {
        if (this instanceof Host) {
            log.debug("physically deleting host: " + this.getName());
            _(EventManager.class).fireEvent(new PhysicalDeleteEvent(this));
            deletePhysically();
        } else {
            if (this.getHost().isDisabled()) {
                log.debug("physically delete because host is disabled");
                deletePhysically();

            } else {
                if (isTrash()) {
                    _(EventManager.class).fireEvent(new PhysicalDeleteEvent(this));
                    deletePhysically();
                } else {
                    _(EventManager.class).fireEvent(new LogicalDeleteEvent(this));
                    Folder trashFolder = getTrashFolder();
                    moveWithRename(this, trashFolder);
                }
            }
        }
    }

    /**
     * Physically delete by calling delete on the namenode
     *
     * Does not commit
     *
     */
    public void deletePhysically() {
        if (log.isTraceEnabled()) {
            log.trace("physically delete item: " + getHref());
        }
        List<Relationship> rels = nameNode.findFromRelations(null);
        if( rels != null ) {
            for( Relationship r : rels ) {
                r.delete();
            }                
        }
        rels = nameNode.findToRelations(null);
        if( rels != null ) {
            for( Relationship r : rels ) {
                r.delete();
            }                
        }
        nameNode.delete();
    }

    /**
     * Will move the resource to the given folder, renaming if necessary
     *
     * @param res
     * @param folder
     */
    private void moveWithRename(BaseResource res, Folder target) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.debug("moveWithRename: res: " + res.getName() + " target: " + target.getHref());
        String name = res.getName();
        boolean isFirst = true;
        while (target.hasChild(name)) {
            name = FileUtils.incrementFileName(name, isFirst);
            isFirst = false;
        }
        log.debug("  - renaming to: " + name);
        res.moveTo(target, name, false);
    }

    @Override
    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    @Override
    public void loadFromXml(Element el) {
        log.warn("loadFromXml");
        super.loadFromXml(el);
        redirect = InitUtils.getValue(el, "redirect");


        Element elGroups = el.getChild("groups");
        if (elGroups != null) {
            log.warn("processing groups");
            GroupService groupService = _(GroupService.class);
            for (Object oGroup : elGroups.getChildren()) {
                Element elGroup = (Element) oGroup;
                String groupName = elGroup.getAttributeValue("group");
                UserGroup group = groupService.getGroup(this, groupName);
                if (group != null) {
                    String roleName = elGroup.getAttributeValue("role");
                    if (!StringUtils.isEmpty(roleName)) {
                        roleName = roleName.trim();
                        try {
                            Role role = Role.valueOf(roleName);
                            this.permissions(true).grant(role, group);
                        } catch (Exception e) {
                            log.error("unknown role: " + roleName, e);
                        }
                    } else {
                        log.warn("empty role name");
                    }
                } else {
                    log.warn("group not found: " + groupName);
                }
            }
        } else {
            log.warn("no groups element");
        }

        Element elRoleRules = el.getChild("roleRules");
        if (elRoleRules != null) {
            this.roleRules = EvalUtils.getEvalDirect(elRoleRules, NS, false, this);
        }
    }

    public final Element toXml(Element el) {
        Element e2 = new Element("res");
        el.addContent(e2);
        populateXml(e2);
        Element elAtts = new Element("attributes");
        e2.addContent(elAtts);
        Map<String, String> mapOfAttributes = new HashMap<>();
        for (Map.Entry<String, String> entry : mapOfAttributes.entrySet()) {
            Element elAtt = new Element("attribute");
            elAtts.addContent(elAtt);
            elAtt.setAttribute("name", entry.getKey());
            elAtt.setText(entry.getValue());
        }

        return e2;
    }

    @Override
    public void populateXml(Element e2) {
        e2.setAttribute("name", getName());
        e2.setAttribute("id", getId().toString());
        e2.setAttribute("nameNodeId", getNameNodeId().toString());
        InitUtils.setString(e2, "redirect", redirect);

        Element elGroups = new Element("groups");
        e2.addContent(elGroups);
        log.trace("add groups");
        for (RoleAndGroup rag : getGroupPermissions()) {
            Element elRag = new Element("group");
            elGroups.addContent(elRag);
            elRag.setAttribute("group", rag.getGroupName());
            elRag.setAttribute("role", rag.getRole().name());
        }


        Element elRels = new Element("relations");
        e2.addContent(elRels);
        Element elFrom = new Element("from");
        elRels.addContent(elFrom);
        for (Relationship r : this.getNameNode().findFromRelations(null)) {
            if (log.isTraceEnabled()) {
                log.trace("relation from: " + r.from().getId() + " -> " + r.to().getId());
            }

            Element elRel = new Element("relationship");
            elFrom.addContent(elRel);
            elRel.setAttribute("relationship", r.relationship());
            NameNode nTo = r.to();
            if (nTo != null) {
                elRel.setAttribute("id", nTo.getId().toString());
                elRel.setAttribute("name", nTo.getName());
            }
        }

        Element elTo = new Element("to");
        elRels.addContent(elTo);
        for (Relationship r : this.getNameNode().findToRelations(null)) {
            Element elRel = new Element("relationship");
            elTo.addContent(elRel);
            elRel.setAttribute("relationship", r.relationship());
            NameNode nFrom = r.from();
            if (nFrom != null) {
                elRel.setAttribute("id", nFrom.getId().toString());
                elRel.setAttribute("name", nFrom.getName());
            }
        }

        Element elRoleRules = new Element("roleRules");
        e2.addContent(elRoleRules);
        EvalUtils.setEvalDirect(elRoleRules, roleRules, NS);


        super.populateXml(e2);
    }

    @Override
    public void save() {
        save(false);
    }
    // used to detect recursive save calls
    private transient boolean isInPreSave;

    public void save(boolean isTrashed) {
        if (isInPreSave) {
            return;
        }
        try {
            isInPreSave = true; // prevent infinite recursion

            isSaved = true;
            preSave();
            fireEvent(new PreSaveEvent(this));
            nameNode.save();
        } finally {
            isInPreSave = false;
            // must allow recursive saves after save has been done, because its
            // possible these might change the state and actually intend this
            // to be changed
            fireEvent(new PostSaveEvent(this));
            afterSave();
        }
    }

    protected void fireEvent(ResourceEvent e) {
        EventManager mgr = _(EventManager.class);
        if (mgr != null) {
            try {
                mgr.fireEvent(e);
            } catch (ConflictException | BadRequestException | NotAuthorizedException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    /**
     * Does not commit or call save
     *
     * @param newName
     */
    public void rename(String newName) {
        LogUtils.info(log, "rename: ", newName);
        nameNode.setName(newName);
    }

    public Text getNameInput() {
        Component c = this.getComponent("name");
        if (c == null) {
            log.warn("no name component: " + this.getPath());
            return null;
        }
        if (c instanceof Text) {
            return (Text) c;
        } else {
            log.warn("name input exists, but is not a " + Text.class + ". Is a: " + c.getClass());
            return null;
        }
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public void init(NameNode nameNode) {
        if (nameNode == null) {
            throw new RuntimeException("init called with null namenode");
        }
        this.nameNode = (RelationalNameNode) nameNode;
        getComponents().init(this);
        initName();
        getValues().init(this);
    }

    /**
     * Need to ensure components know who their parents are. this information is
     * not persisted, and is only held in transient variables which may be lost
     * if passivated to a disk cache
     *
     */
    protected void initComponents() {
        getComponents().init(this);
        getValues().init(this);
    }

    @Override
    public void loadFromXml(Element el, Map<String, String> params) {
        loadFromXml(el);
    }

    @Override
    public Element toXml(Element el, Map<String, String> params) {
        return toXml(el);
    }

    /**
     * The folder which contains this physical resource
     *
     * @return
     */
    @Override
    public Folder getParent() {
        if (nameNode == null) {
            return null;
        }
        if (nameNode.getParent() == null) {
            log.warn("No parent for " + getName() + " id:" + getId());
            return null;
        }
        return (Folder) nameNode.getParent().getData();
    }

    @Override
    public String getName() {
        if (nameNode == null) {
            log.warn("Namenode has not been set, init has not been called");
            return null;
        } else {
            return nameNode.getName();
        }
    }

    @Override
    public String getRealm() {
        return getHost().getName();
    }

    @Override
    public Date getModifiedDate() {
        Date thisModDate = nameNode.getModifiedDate();
        return thisModDate;
    }

    public String getModifiedDateFormatted() {
        return DateDef.sdfDateAndTime.get().format(getModifiedDate());
    }

    public Long getModifiedDateAsLong() {
        Date dt = getModifiedDate();
        if (dt == null) {
            return null;
        } else {
            return dt.getTime();
        }
    }

    @Override
    public Date getCreateDate() {
        return nameNode.getCreatedDate();
    }

    @Override
    public UUID getId() {
        return id;
    }

    public Web getParentWeb() {
        Folder f = getParent();
        if (f == null) {
            log.debug("no parent: " + getName());
            return null;
        }
        return f.getWeb();
    }

    void setContent(InputStream in) {
        if (in == null) {
            return;
        }
        throw new UnsupportedOperationException("Not implemented for base class");
    }

    /**
     * Does nothing. Place holder. Called immediately before saving the node
     */
    protected void preSave() {
        ITemplate template = this.getTemplate();
        if (template != null) {
            template.onBeforeSave(this);
        } else {
            log.debug("no template, so can't run afterSave");
        }
    }

    /**
     * Called immediatley after saving the node, if no exception
     */
    protected void afterSave() {
        boolean didChange = false;
        for (ComponentValue cv : this.getValues().values()) {
            didChange = didChange || cv.afterSave();
        }
        if (didChange) {
            this.save(); // careful, watch for recursion
        }
        log.trace("afterSave: " + this.getTemplateName());
        ITemplate template = this.getTemplate();
        if (template != null) {
            template.onAfterSave(this);
        } else {
            log.debug("no template, so can't run afterSave");
        }
    }

    public Folder getTrashFolder() {
        return getParent().getTrashFolder();
    }

    private Relationship getRelationNode(String relationName) {
        List<Relationship> list = nameNode.findFromRelations(relationName);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Relationship r = list.get(0);
        return r;
    }

    public BaseResource getRelation(String relationName) {
        Relationship r = getRelationNode(relationName);
        if (r == null) {
            return null;
        }
        NameNode toNode = r.to();
        if (toNode == null) {
            log.error("Found a relation with no to node. from resource: " + this.getPath() + " - " + this.getNameNodeId() + "  relation: " + relationName);
            return null;
        }
        BaseResource toRes = (BaseResource) toNode.getData();
        return toRes;
    }

    public BaseResourceList getRelations(String relationName) {
        BaseResourceList resList = new BaseResourceList();
        List<Relationship> list = nameNode.findFromRelations(relationName);
        if (list == null || list.isEmpty()) {
            return resList;
        }

        for (Relationship r : list) {
            BaseResource res = (BaseResource) r.to().getData();
            resList.add(res);
        }
        return resList;
    }

    public BaseResourceList getToRelations(String relationName) {
        BaseResourceList resList = new BaseResourceList();
        List<Relationship> list = nameNode.findToRelations(relationName);
        if (list == null || list.isEmpty()) {
            return resList;
        }

        for (Relationship r : list) {
            NameNode from = r.from();
            if (from != null) {
                BaseResource res = (BaseResource) from.getData();
                resList.add(res);
            }
        }
        return resList;
    }

    /**
     * Create a relationship. Relationships should generaly be one-to-many, and
     * should be made from the many side to the parent.
     *
     * This will remove any previous relationship with the given name from this
     * node
     *
     * Eg if an invoice relates to a customer, you should call
     * createRelationship on the invoice, providing the customer as the To
     * object
     *
     * @param relationName
     * @param to
     */
    public void createRelationship(String relationName, BaseResource to) {
        removeRelationship(relationName);
        Relationship r = this.nameNode.makeRelation(to.nameNode, relationName);
        to.nameNode.onNewRelationship(r);
    }

    /**
     * Just create a relationship, without removing any previous
     *
     * @param relationName
     * @param to
     */
    public void createManyToManyRelationship(String relationName, BaseResource to) {
        Relationship r = this.nameNode.makeRelation(to.nameNode, relationName);
        to.nameNode.onNewRelationship(r);
    }

    public void removeRelationship(String relationName) {
        Relationship r = getRelationNode(relationName);
        if (r == null) {
            return;
        }
        r.delete();
    }

    public void removeRelationship(String relationName, UUID id) {
        List<Relationship> list = nameNode.findFromRelations(relationName);
        if (list == null) {
            return;
        }
        for (Relationship r : list) {
            if (r.to().getId().equals(id)) {
                r.delete();
            }
        }
    }

    public BaseResource findByNameNodeId(UUID id) {
        NameNode nn = this.vfs().get(id);
        return (BaseResource) nn.getData();
    }

    /**
     * Is this resource 'trashed', ie in the trash folder
     *
     * @return
     */
    public boolean isTrash() {
        for (Templatable t : this.getParents()) {
            if (t.getName().equals("Trash")) {
                return true;
            }
        }
        return false;
    }

    public Permissions permissions() {
        return permissions(false);
    }

    public Permissions permissions(boolean create) {
        NameNode nnPermissions = this.nameNode.child(Permissions.NAME_NODE_KEY);
        Permissions p;
        if (nnPermissions == null) {
            if (create) {
                p = new Permissions();
                nnPermissions = this.nameNode.add(Permissions.NAME_NODE_KEY, p);
                nnPermissions.save();
            } else {
                p = null;
            }
        } else {
            DataNode dn = nnPermissions.getData();
            if (dn != null) {
                if (dn instanceof Permissions) {
                    p = (Permissions) nnPermissions.getData();
                } else {
                    log.warn("found: " + Permissions.NAME_NODE_KEY + " but is not a Permissions class. Is a: " + dn.getClass() + " resource nnid:" + this.getNameNodeId());
                    p = null;
                }
            } else {
                log.warn("found: " + Permissions.NAME_NODE_KEY + " but it has no data node associated. resource nnid:" + this.getNameNodeId());
                p = null;
            }
        }
        return p;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public HrefList getPrincipalCollectionHrefs() {
        HrefList list = new HrefList();
        IUser user = _(CurrentUserService.class).getOnBehalfOf();
        if (user instanceof Templatable) {
            Templatable t = (Templatable) user;
            list.add(t.getHref());
        }
        return list;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getPrincipalURL() {
        IUser owner = getCreator();
        if (owner == null) {
            return null;
        } else {
            return owner.getHref();
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        System.out.println("getAccessControlList");
        Map<Principal, List<Priviledge>> map = new HashMap<>();
        Permissions perms = this.permissions();

        if (perms != null) {
            System.out.println("perms: " + perms.size());
            AuthoringPermissionService aps = _(AuthoringPermissionService.class);
            for (Permission p : perms) {
                Principal principal = p.getGrantee();
                List<Priviledge> privs = map.get(principal);
                if (privs == null) {
                    privs = new ArrayList<>();
                    map.put(principal, privs);
                }
                privs.addAll(aps.toPriviledges(p.getRole()));
            }
        } else {
            System.out.println("perms is null");
        }
        System.out.println("map: " + map.size());
        return map;
    }

    @Override
    public void setAccessControlList(Map<Principal, List<Priviledge>> privs) {
        throw new RuntimeException("Not implemented");
//		for( Entry<Principal, List<Priviledge>> entry : privs.entrySet()) {
//			Principal p = entry.getKey();
//		}
    }

    /**
     * {@inheritDoc}
     *
     * @param auth
     * @return
     */
    @Override
    public List<Priviledge> getPriviledges(Auth auth) {
        Permissions perms = this.permissions();
        Set<Priviledge> privs = EnumSet.noneOf(Priviledge.class);
        AuthoringPermissionService aps = _(AuthoringPermissionService.class);
        IUser current = _(CurrentUserService.class).getSecurityContextUser();
        if (perms != null) {
            for (Permission p : perms) {
                if (p.getGrantee().isOrContains(current)) {
                    privs.addAll(aps.toPriviledges(p.getRole()));
                }
            }
        }
        List<Priviledge> privsList = new ArrayList<>(privs);
        return privsList;
    }

    /**
     * Creates a relation from this to the user with name "creator"
     *
     * @param user
     */
    public void setCreator(User user) {
        this.creator = user;
        _(CreatorService.class).setCreator(user, this);
    }

    /**
     * looks for the relation created by setCreator and returns the associated
     * resource, which must be a User
     *
     * @return
     */
    public User getCreator() {
        // Something dodgy going on here. Seem to get different results wihthout
        // the transient variable
        if (this.creator == null) {
            this.creator = (User) _(CreatorService.class).getCreator(this);
        }
        return creator;
    }

    public String getCreatorName() {
        User u = getCreator();
        if (u == null) {
            return null;
        }
        return u.getName();
    }

    public String getCreatorExternalEmail() {
        User u = getCreator();
        if (u == null) {
            return null;
        }
        return u.getExternalEmailText();
    }

    public UUID getCreatorNameNodeId() {
        return creatorNameNodeId;
    }

    public void setCreatorNameNodeId(UUID creatorNameNodeId) {
        this.creatorNameNodeId = creatorNameNodeId;
    }

    public String getMagicNumber() {
        return this.getNameNodeId().hashCode() + "";
    }

    /**
     * Get the email address persisted in a child name node for the given
     * category
     *
     * @param emailCategory - eg default, personal, business
     * @return
     */
    public String getExternalEmailTextV2(String emailCategory) {
//        log.debug( "getExternalEmailTextV2: " + emailCategory + " nnid: " + this.getNameNodeId());
        NameNode nEmailContainer = this.nameNode.child("_email_" + emailCategory);
        if (nEmailContainer == null) {
            //log.warn( "no container" );
            return null;
        }
        for (NameNode child : nEmailContainer.children()) {
            DataNode childData = child.getData();
            if (childData instanceof EmailAddress) {
                return child.getName();
            }
        }
        return null;
    }

    /**
     * Stores the email address in a child namenode for fast efficient lookups
     *
     * @param emailCategory - the category of this email address. Eg default,
     * personal, business
     * @param email
     */
    public void setExternalEmailTextV2(String emailCategory, String email) {
//        log.debug( "setExternalEmailTextV2: " + emailCategory + " email:" + email);
        NameNode nEmailContainer = this.nameNode.child("_email_" + emailCategory);
        if (nEmailContainer == null) {
            nEmailContainer = nameNode.add("_email_" + emailCategory, new EmptyDataNode());
            nEmailContainer.save();
        }
        List<NameNode> children = new ArrayList<>(nEmailContainer.children());
        for (NameNode child : children) {
            DataNode childData = child.getData();
            if (childData instanceof EmailAddress) {
                child.delete();
            }
        }
        if (email != null && email.length() > 0) {
            NameNode nEmail = nEmailContainer.add(email, new EmailAddress());
            nEmail.save();
        }
    }

    public List<Comment> getComments() {
        return _(CommentService.class).comments(this.nameNode);
    }

    public int getNumComments() {
        List<Comment> list = getComments();
        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }

    public void setNewComment(String s) throws NotAuthorizedException {
        _(CommentService.class).newComment(this.nameNode, s);
    }

    /**
     * This is just here to make newComment a bean property
     *
     * @return
     */
    @BeanProperty(readRole = Role.AUTHENTICATED, writeRole = Role.AUTHENTICATED)
    public String getNewComment() {
        return null;
    }

    @Override
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException, LockedException {
        LockResult lr = _(ClydeLockManager.class).lock(timeout, lockInfo, this);
        commit();
        return lr;
    }

    /**
     * Renew the lock and return new lock info
     *
     * @param token
     * @return
     */
    @Override
    public LockResult refreshLock(String token) throws NotAuthorizedException, PreConditionFailedException {
        LockResult lr = _(ClydeLockManager.class).refresh(token, this);
        commit();
        return lr;
    }

    @Override
    public void unlock(String tokenId) throws NotAuthorizedException, PreConditionFailedException {
        _(ClydeLockManager.class).unlock(tokenId, this);
        commit();
    }

    @Override
    public LockToken getCurrentLock() {
        return _(ClydeLockManager.class).getCurrentLock(this);
    }

    /**
     * Allows user-agent defined modified dates. Defaults to modifiedDate if not
     * set
     *
     * @return
     */
    public Date getTimestamp() {
        if (timestamp == null) {
            return getModifiedDate();
        }
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Evaluatable getRoleRules() {
        return roleRules;
    }

    public void setRoleRules(Evaluatable roleRules) {
        this.roleRules = roleRules;
    }

    public List<RoleAndGroup> getGroupPermissions() {
        if (groupPermissions == null) {
            groupPermissions = new ArrayList<>();
        }
        return groupPermissions;
    }

    public static class RoleAndGroup implements Serializable {

        private static final long serialVersionUID = 1L;
        private final Role role;
        private final String systemUserGroupName;

        public RoleAndGroup(Role role, String systemUserGroupName) {
            this.role = role;
            this.systemUserGroupName = systemUserGroupName;
        }

        public Role getRole() {
            return role;
        }

        public String getGroupName() {
            return systemUserGroupName;
        }

        public boolean equalTo(RoleAndGroup rag) {
            return rag.getGroupName().equals(systemUserGroupName) && rag.getRole().equals(role);
        }

        @Override
        public String toString() {
            return "Grant " + role.name() + " on " + systemUserGroupName;
        }
    }

    public Relations getRelations() {
        if (relations == null) {
            relations = new Relations(this);
        }
        return relations;
    }

    public void share(String otherPrincipal, Role role) {
        System.out.println("share: " + otherPrincipal + " - " + role);
        User user = _(UserLocator.class).findUser(getHost(), otherPrincipal);
        if (user != null) {
            Permissions perms = permissions(true);
            perms.grant(role, user);
            commit();
        } else {
            throw new RuntimeException("User not found: " + otherPrincipal);
        }
    }

    @FormAction(requiredRole = Role.AUTHOR)
    public void share(@FormParameter(name = "friend") String otherPrincipal, @FormParameter(name = "role") String sRole) {
        Role role = Role.valueOf(sRole);
        share(otherPrincipal, role);
    }

    @FormAction(requiredRole = Role.AUTHOR)
    public void unshare(String otherPrincipal) {
        User user = _(UserLocator.class).findUser(getHost(), otherPrincipal);
        if (user != null) {
            Permissions perms = permissions(true);
            perms.revokeAll(user);
            commit();
        } else {
            throw new RuntimeException("User not found: " + otherPrincipal);
        }
    }

    /**
     * get a list of all shares for this folder. The map is keyed on the path
     * identifying the Principal (group or user), and a list of Roles for each
     *
     * @return
     */
    @BeanProperty
    public Collection<ShareInfo> getShares() {
        System.out.println("getShares");
        Map<String, ShareInfo> map = new HashMap<String, ShareInfo>();
        Permissions perms = this.permissions();

        if (perms != null) {
            System.out.println("perms: " + perms.size());
            for (Permission p : perms) {
                Principal principal = p.getGrantee();
                if (principal instanceof IUser) {
                    IUser user = (IUser) principal;
                    String key = user.getName();
                    ShareInfo shareInfo = map.get(key);
                    if (shareInfo == null) {
                        shareInfo = new ShareInfo(key, user.getHref());
                        map.put(key, shareInfo);
                    }
                    shareInfo.getRoles().add(p.getRole().toString());
                }
            }
        } else {
            System.out.println("perms is null");
        }
        System.out.println("map: " + map.size());
        return map.values();
    }

    public Date getSourceModDate() {
        return sourceModDate;
    }

    public void setSourceModDate(Date sourceModDate) {
        this.sourceModDate = sourceModDate;
    }

    @Override
    public List<WebResource> getWebResources() {
        return webResources;
    }

    public void setWebResources(List<WebResource> webResources) {
        this.webResources = webResources;
    }
}
