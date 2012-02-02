package com.ettrema.web;

import com.ettrema.forms.FormAction;
import com.ettrema.forms.FormParameter;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.values.HrefList;
import com.ettrema.utils.ClydeUtils;
import java.util.Arrays;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.creation.ResourceCreator;
import com.bradmcevoy.common.Path;
import com.ettrema.event.PutEvent;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableCollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.io.BufferingOutputStream;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.binary.StateTokenManager;
import com.ettrema.utils.LogUtils;
import com.ettrema.web.children.ChildFinder;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.TemplateSelect;
import com.ettrema.web.component.Text;
import com.ettrema.web.component.TypeMapping;
import com.ettrema.web.component.TypeMappingsComponent;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.jdom.Element;
import static com.ettrema.context.RequestContext.*;

/**
 * Represents a folder in the Clyde CMS. Implements collection method interfaces
 *
 * Implements DeletableCollectionResource so that delete is not called recursively from
 * milton.
 *
 * @author brad
 */
@BeanPropertyResource("clyde")
public class Folder extends BaseResource implements com.bradmcevoy.http.FolderResource, XmlPersistableResource, DeletableCollectionResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Folder.class);
    private static final long serialVersionUID = 1L;

    static Folder find(Templatable t) {
//        log.debug( "find: " + t.getClass() + " - " + t.getName());
        if (t.getParent() == null) {
            return null;
        }
        if (t.getParent() instanceof Folder) {
            return (Folder) t.getParent();
        }
        return find(t.getParent());
    }
    protected boolean secureRead; // deprecated
    protected Boolean secureRead2; // allow nulls
    /**
     * The href of a thumb nail image for this folder, or null if none exists.
     */
    private String thumbHref;
    /**
     * The allowed and disallowed specs
     */
    TemplateSpecs templateSpecs = new TemplateSpecs();
    /**
     * Whether or not resources should be version controlled, if supported by
     * ClydeBinaryService
     */
    private Boolean versioningEnabled;
    //private transient List<TransientNameNode> transientNameNodes;
    /**
     * performance optimisation, record the fact that there is (or may be) at least one 
     * linked folder which links to this one. This allows us to update binary crc's
     * on LinkedFolder's without always checking to see if there are any.
     */
    private boolean linkedFolders;
    
    private Long counter;

    /** Create a root folder
     */
    public Folder() {
        super();
    }

    public Folder(Folder parentFolder, String newName) {
        super(null, parentFolder, newName);
    }

    @Override
    public String getDefaultContentType() {
        return null;
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        Folder fNew = (Folder) super.copyInstance(parent, newName);
        fNew.secureRead = this.secureRead;
        fNew.secureRead2 = this.secureRead2;
        fNew.versioningEnabled = this.versioningEnabled;
        if (this.templateSpecs != null) {
            fNew.templateSpecs.addAll(this.templateSpecs);
        }
        return fNew;
    }

    /**
     *
     * @return - the persisted thumb href
     */
    public String getThumbHref() {
        return thumbHref;
    }

    public void setThumbHref(String thumbHref) {
        this.thumbHref = thumbHref;
    }

    /**
     * If this folder is not defined as secureRead, recursively check parents until Web
     * 
     * @return
     */
    public boolean isSecureRead() {
        if (log.isTraceEnabled()) {
            log.trace("isSecureRead: " + this.getName() + " - secureRead:" + secureRead + " - secureRead2" + secureRead2);
        }
        if (secureRead) {
            return true;
        }
        if (secureRead2 != null) {
            return secureRead2.booleanValue();
        }
        if (this.getParent() == null) {
            return false;
        }
        return this.getParent().isSecureRead(); // overridden by Host
    }

    public Boolean isSecureRead2() {
        return secureRead2;
    }

    public void setSecureRead(Boolean b) {
        LogUtils.trace(log, "setSecureRead", b);
        this.secureRead2 = b;
        if (b != null) {
            this.secureRead = b;
        }
    }

    public boolean getHasChildren() {
        return this.getNameNode().children().size() > 0;
    }

    /**
     * adds an integer, if necessary, to ensure the name is unque in this folder
     *
     * @param name
     * @return - a unique name in this folder, equal to or prefixed by the given name
     */
    public String buildNonDuplicateName(String name) {
        if (!childExists(name)) {
            return name;
        }
        int i = 1;
        while (childExists(name + i)) {
            i++;
        }
        return name + i;
    }

    public boolean childExists(String name) {
        return this.child(name) != null;
    }

    @Override
    public String getTitle() {
        String s = getTitleNoName();
        if (s == null) {
            CommonTemplated page = (CommonTemplated) this.getIndexPage();
            if (page != null) {
                if (!(page instanceof SubPage)) { // subpages generally dont add information, they will use the parent folder's info
                    s = page.getTitle();
                } else {
                    s = getName();
                }
            } else {
                s = this.getName();
            }
        }
        if (s == null || s.trim().length() == 0) {
            s = getName();
        }
        return s;
    }

    @Override
    public Resource getChildResource(String childName) {
        Resource f = child(childName);
        if (f != null) {
            return f;
        }

        return super.getChildResource(childName);
    }

    public long getTotalSize() {
        long size = 0;
        for (Resource r : this.getChildren()) {
            if (r instanceof Folder) {
                Folder f = (Folder) r;
                size += f.getTotalSize();
            } else {
                if (r instanceof GetableResource) {
                    GetableResource gr = (GetableResource) r;
                    Long resLength = gr.getContentLength();
                    if (resLength != null) {
                        size += resLength.longValue();
                    }
                }
            }
        }
        return size;
    }

    public List<Folder> getSubFolders() {
        List<Folder> list = new ArrayList<Folder>();
        for (Resource res : this.getChildren()) {
            if (res instanceof Folder) {
                list.add((Folder) res);
            }
        }
        return list;
    }

    public Folder getSubFolder(String name) {
        Resource res = this.child(name);
        if (res == null) {
            log.debug("sub folder not found: " + name);
            return null;
        } else {
            if (res instanceof Folder) {
                return (Folder) res;
            } else {
                log.debug("child is not of type folder. is: " + res.getClass());
                return null;
            }
        }
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        setSecureRead(InitUtils.getNullableBoolean(el, "secureRead"));
        versioningEnabled = InitUtils.getBoolean(el, "versioningEnabled");
        thumbHref = InitUtils.getValue(el, "thumbHref");
        String s = el.getAttributeValue("allowedTemplates");
        templateSpecs = TemplateSpecs.parse(s);
    }

    @Override
    public void populateXml(Element e2) {
        log.trace("populateXml");
        super.populateXml(e2);
        InitUtils.set(e2, "secureRead", secureRead2);
        InitUtils.set(e2, "versioningEnabled", versioningEnabled);
        InitUtils.setString(e2, "thumbHref", thumbHref);
        InitUtils.setLong(e2, "binaryStateToken", getBinaryStateToken());
        if (templateSpecs == null) {
            templateSpecs = new TemplateSpecs("");
        }
        e2.setAttribute("allowedTemplates", templateSpecs.format());

    }

    @Override
    public boolean is(String type) {
        if (super.is(type)) {
            return true;
        } else {
            return type.equals("folder");
        }
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        // Don't have content for folders
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    /**
     * Copy this folder to the given destination and with the given name
     * 
     * Recursively copies children
     * 
     * Does not commit
     * 
     * @param dest
     * @param name
     */
    @Override
    public void _copyTo(Folder dest, String name) {
        Folder newFolder = (Folder) copyInstance(dest, name);
        newFolder.templateSelect = (TemplateSelect) newFolder.componentMap.get("template");
        newFolder.save();
        log.debug("created new folder: " + newFolder.getHref());
        for (Resource child : this.getChildren()) {
            if (child instanceof BaseResource) {
                BaseResource resChild = (BaseResource) child;
                log.debug("copying child: " + resChild.getName());
                resChild._copyTo(newFolder);
            }
        }
    }

    @Override
    public void onDeleted(NameNode nameNode) {
        Folder parent = getParent();
        if (parent != null) {
            parent.onRemoved(this);
        }
    }

    @Override
    public CollectionResource createCollection(final String newName) throws ConflictException, NotAuthorizedException, BadRequestException {
        log.debug("createCollection: " + newName + " in folder with id: " + this.getNameNodeId());
        return createCollection(newName, true);
    }

    public CollectionResource createCollection(String newName, boolean commit) throws ConflictException, NotAuthorizedException, BadRequestException {
        Resource res;
        BaseResource child = childRes(newName);
        if (child != null) {
            throw new ConflictException(child, "An item already exists named: " + newName + " - " + child.getClass() + " in " + this.getHref());
        }
        try {
            res = createNew_notx(newName, null, null, "folder");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if (res instanceof CollectionResource) {
            if (commit) {
                commit();
            }
            return (CollectionResource) res;
        } else {
            throw new RuntimeException("did not create a collection. created a: " + res.getClass().getName());
        }
    }

    @Override
    public Resource child(String name) {
        return _(ChildFinder.class).find(name, this);
    }

    public BaseResource childRes(String name) {
        //log.trace( "childRes: " + name + " node: " + getNameNodeId() + " this folder: " + this.getName());
        if (getNameNode() == null) {
            throw new NullPointerException("nameNode is null. This can often happen if the instance is being used outside of its session (ie has become detached)");
        }
        NameNode childNode = getNameNode().child(name);
        if (childNode != null) {
            try {
                DataNode dn = childNode.getData();
                if (dn == null) {
                    return null;
                } else if (dn instanceof BaseResource) {
                    return (BaseResource) dn;
                } else {
                    return null;
                }
            } catch (Throwable e) {
                log.error("couldnt load resource with id: " + childNode.getId(), e);
                return null;
            }
        } else {
//            if( transientNameNodes != null ) {
//                for( TransientNameNode nn : transientNameNodes ) {
//                    if( nn.getName().equals( name ) ) {
//                        DataNode dn = nn.getData();
//                        if( dn == null ) {
//                            return null;
//                        } else if( dn instanceof BaseResource ) {
//                            return (BaseResource) dn;
//                        } else {
//                            return null;
//                        }
//                    }
//                }
//            }
            return null;
        }
    }

    public <T> T findFirst(Class<T> c) {
        for (NameNode n : getNameNode().children()) {
            if (c.isAssignableFrom(n.getDataClass())) {
                return (T) n.getData();
            }
        }
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() {
        List<Templatable> children = getChildren(null);
        return children;
    }

    public List<Templatable> getChildren(String isA) {
        return getChildren(isA, null);
    }

    public List<Templatable> getChildren(String isA, String except) {
        List<Templatable> children = new BaseResourceList();
        NameNode nThis = getNameNode();
        if (nThis != null) {
            List<NameNode> list = nThis.children();
            if (list != null) {
                for (NameNode n : nThis.children()) {
                    DataNode dn = n.getData();
                    if (dn != null && dn instanceof BaseResource) {
                        BaseResource res = (BaseResource) dn;
                        if (isA == null || res.is(isA)) {
                            if (!res.getName().equals(except)) {
                                children.add(res);
                            }
                        }
                    }
                }
            }
        } else {
            log.debug("null namenode");
        }


        if (isA == null && except == null) {
            List<Templatable> subPages = _(ChildFinder.class).getSubPages(this);
            if (subPages != null) {
                for (Templatable t : subPages) {
                    children.add(t);
                }
            }
        }

        Collections.sort(children);
        return children;
    }

    public List<Templatable> children(String template) {
        return getChildren(template);
    }

    public BaseResourceList getPagesRecursive() {
        BaseResourceList list = new BaseResourceList();
        appendChildrenRecursive(list, 0, null, null);
        return list;
    }

    public BaseResourceList pagesRecursive(int limit) {
        BaseResourceList list = new BaseResourceList();
        appendChildrenRecursive(list, 0, null, limit);
        return list;
    }

    public BaseResourceList pagesRecursive(int limit, String type) {
        BaseResourceList list = new BaseResourceList();
        appendChildrenRecursive(list, 0, type, limit);
        return list;
    }

    private void appendChildrenRecursive(List list, int depth, String type, Integer limit) {
        if (depth > 5) {
            log.trace("exceeded max depth");
            return;
        }
        if (limit != null) {
            if (list.size() > limit) {
                return;
            }
        }

        for (Resource r : this.getChildren()) {
            if (r instanceof Folder) {
                Folder f = (Folder) r;
                if (!f.getName().equals("templates")) {
                    f.appendChildrenRecursive(list, depth++, type, limit);
                }
            } else if (r instanceof BaseResource) {
                BaseResource p = (BaseResource) r;
                if (type == null || p.is(type)) {
                    list.add(p);
                    if (limit != null) {
                        if (list.size() > limit) {
                            return;
                        }
                    }
                }
            } else {
                // do nothing
            }
        }
    }

    public BaseResourceList thumbsList(int limit) {
        return thumbsList(limit, "_sys_thumb");
    }

    public BaseResourceList thumbsList(int limit, String thumbSpec) {
        BaseResourceList list = new BaseResourceList();
        appendThumbsRecursive(list, limit, thumbSpec);
        return list;
    }

    private void appendThumbsRecursive(List list, int limit, String thumbSpec) {
        if (list.size() >= limit) {
            log.trace("exceeded max limit");
            return;
        }

        Folder thumbsFolder = this.thumbs(thumbSpec);
        if (thumbsFolder != null) {
            for (Templatable r : thumbsFolder.getChildren("image")) {
                if (r instanceof BinaryFile) {
                    list.add(r);
                }
                if (list.size() >= limit) {
                    return;
                }
            }
        }

        for (Resource r : this.getChildren()) {
            if (r instanceof Folder) {
                Folder f = (Folder) r;
                if (!f.getName().equals("templates")) {
                    f.appendThumbsRecursive(list, limit, thumbSpec);
                }
            }
        }
    }

    public BaseResourceList getFoldersRecursive() {
        BaseResourceList list = new BaseResourceList();
        appendFoldersRecursive(list, 0);
        return list;
    }

    private void appendFoldersRecursive(List list, int depth) {
        if (depth > 5) {
            return;
        }
        for (Resource r : this.getChildren()) {
            if (r instanceof Folder) {
                Folder f = (Folder) r;
                list.add(r);
                if (!f.getName().equals("templates")) {
                    f.appendFoldersRecursive(list, depth++);
                }
            } else {
                // do nothing
            }
        }
    }

    @Override
    public Resource createNew(String newName, InputStream in, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        if (newName.equals(NewPage.AUTO_NAME)) {
            String headerName = HttpManager.request().getHeaders().get("X-Filename");
            Map<String, String> params = null;
            if (headerName != null) {
                params = new HashMap<String, String>();
                params.put("name", headerName);
            }
            newName = NewPage.findAutoName(this, params);
            LogUtils.trace(log, "autoname, with header name", headerName, "final name", newName);
        }
        Resource res = createNew_notx(newName, in, length, contentType);
        fireEvent(new PutEvent((BaseResource) res));
        commit();
        return res;
    }

    public Resource createNew_notx(String newName, InputStream in, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        checkHost();
        Resource rExisting = child(newName);
        if (rExisting != null) {
            if (rExisting instanceof Replaceable) {
                log.debug("PUT to a replaceable resource. replacing content...");
                Replaceable replaceTarget = (Replaceable) rExisting;
                doReplace(replaceTarget, in, length);
                return rExisting;
            } else if (rExisting instanceof BaseResource) {
                log.debug("deleting existing item:" + rExisting.getName());
                ((BaseResource) rExisting).delete();
                return doCreate(newName, in, length, contentType);
            } else {
                throw new RuntimeException("Cannot delete: " + rExisting.getClass().getName());
            }
        } else {
            log.debug("creating new item");
            return doCreate(newName, in, length, contentType);
        }

    }

    private void checkHost() throws ConflictException {
        Host h = this.getHost();
        if (h.isDisabled()) {
            log.warn("Attempt to put to a disabled host: " + h.getName());
            throw new ConflictException(this);
        }
    }

    public Folder getOrCreateFolder(ComponentValue cv) throws ConflictException, NotAuthorizedException, BadRequestException {
        return getOrCreateFolder(cv.getValue().toString());

    }

    /**
     * Return a folder if one exists with the given name. Otherwise, and if
     * not resource exists, create a generic folder.
     * 
     * If a resource does exist which is not a folder it will throw a ConflictException
     * 
     * @param name
     * @return
     */
    public Folder getOrCreateFolder(String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        Resource res = this.child(name);
        if (res == null) {
            return (Folder) createCollection(name, false); // will commit elsewhere
        } else {
            if (res instanceof Folder) {
                return (Folder) res;
            } else {
                log.error("Couldnt create folder, because a resource exists with the same name but is not a folder: " + res.getName() + " - " + res.getClass());
                throw new ConflictException(res);
            }
        }
    }

    public Templatable createPage(String name, String template) {
        ITemplate t = this.getTemplate(template);
        return t.createPageFromTemplate(this, name);
    }

    public Resource doCreate(String newName) {
        try {
            return doCreate(newName, null, null, null);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Create a resource from a template
     *
     * Does a save, but does not commit
     * 
     * @param name - the name of the resource to create. May be null, which will cause
     * a unique name to be generated
     * @param templateName - the name of the template to assign to the resource. Is validated.
     * @return
     */
    public Resource create(String name, String templateName) {
        if (name == null) {
            name = ClydeUtils.getDateAsNameUnique(this);
        }
        if (this.childExists(name)) {
            throw new RuntimeException("A file already exists called: " + name);
        }
        ITemplate t = getTemplate(templateName);
        if (t == null) {
            throw new RuntimeException("No such template: " + templateName);
        }
        BaseResource res = t.createPageFromTemplate(this, name);
        res.save();
        return res;
    }

    public Resource doCreate(String newName, InputStream in, Long length, String contentType) throws ReadingException, WritingException {
        log.debug("doCreate: " + newName + " contentType: " + contentType);
        BaseResource res = null;
        Iterable<Path> contentTypePaths;
        if (contentType == null || contentType.length() == 0 || contentType.equals("application/octet-stream")) {
            contentTypePaths = ContentTypeUtil.getContentTypeList(newName);
        } else {
            contentTypePaths = Arrays.asList(Path.path(contentType));
        }

        List<TypeMapping> typeMappings = getTypeMappings();

        if (typeMappings != null) {
            for (Path p : contentTypePaths) {
                for (TypeMapping tm : typeMappings) {
                    if (tm.contentType.equals(p.toString())) {
                        ITemplate t = getTemplate(tm.templateName);
                        if (t == null) {
                            log.warn("Couldnt find template associated with type mapping: type mapping: " + tm.contentType + " template: " + tm.templateName);
                        } else {
                            log.debug("found template: " + t.getName() + " from content type: " + tm.contentType);
                            res = t.createPageFromTemplate(this, newName, in, length);
                            res.save();
                            break;
                        }
                    }
                }
            }
        } else {
            for (Path p : contentTypePaths) {
                contentType = p.toString();
            }
        }
        if (res == null) {
//            log.debug("res was not created through type mappings. falling back to default");
            res = defaultCreateItem(contentType, in, newName, length);
        }

        return res;
    }

    public List<TypeMapping> getTypeMappings() {
        Component c = this.getComponent("typeMappings");
        List<TypeMapping> typeMappings = null;
        if (c != null) {
            if (c instanceof TypeMappingsComponent) {
                typeMappings = ((TypeMappingsComponent) c).getValue();
            } else {
                throw new IllegalArgumentException("typeMappings component must be of type: " + TypeMappingsComponent.class.getName());
            }
        }
        return typeMappings;
    }

    public Folder thumbs(String thumbSpec) {
        return thumbs(thumbSpec, false);
    }

    public Folder thumbs(String thumbSpec, boolean create) {
        String name = thumbSpec + "s";
        Resource res = child(name);
        if (res == null) {
            if (create) {
                Folder f = new Folder(this, name);
                f.save();
                return f;
            } else {
                return null;
            }
        } else {
            if (res instanceof Folder) {
                Folder f = (Folder) res;
                return f;
            } else {
                log.warn("File of same name as thumbs folder exists: " + name);
                return null;
            }
        }
    }

    /** Called by a child object when it is constructed
     *
     *  Create and return a suitable NameNode
     */
    NameNode onChildCreated(String newName, BaseResource baseResource) {
//        NameNode nn = nameNode.add(newName,baseResource);
//        if( transientNameNodes == null ) {
//            transientNameNodes = new ArrayList<TransientNameNode>();
//        }
//        TransientNameNode nn = new TransientNameNode( newName, baseResource );
//        transientNameNodes.add( nn );
//        return nn;
        return nameNode.add(newName, baseResource);
    }

    public boolean hasChild(String name) {
        return (child(name) != null);
    }

    public TemplateSpecs getTemplateSpecs() {
        return templateSpecs;
    }

    public void setTemplateSpecs(TemplateSpecs s) {
        this.templateSpecs = s;
    }

    public void setAllowedTemplates(String s) {
        this.templateSpecs = TemplateSpecs.parse(s);
    }

    public List<String> getAllowedTemplateNames() {
        List<Template> templates = getAllowedTemplates();
        List<String> names = new ArrayList<String>();
        if (templates != null) {
            for (Template t : templates) {
                names.add(t.getName());
            }
        }
        return names;
    }

    public List<Template> getAllowedTemplates() {
        LogUtils.trace(log, "getAllowedTemplates: ", this.getName());
        if (templateSpecs == null || templateSpecs.size() == 0) {
            Component c = this.getComponent("allowedTemplates");
            if (c != null) {
                if (c instanceof Text) {
                    log.trace("templates defined by template");
                    Text t = (Text) c;
                    String s = t.getValue();
                    LogUtils.trace(log, "got from template: ", s);
                    TemplateSpecs specs = TemplateSpecs.parse(s);
                    List<Template> list = specs.findAllowedDirect(this);
                    LogUtils.trace(log, "allowed template size: ", list.size());
                    return list;
                } else {
                    log.warn("not a compatible component: " + c.getClass());
                }
            }
            Folder parent = getParent();
            if (parent != null) {
                return getParent().getAllowedTemplates();
            } else {
                return null;
            }
        } else {
            log.trace("templates defined directly on this instance");
            return templateSpecs.findAllowed(this);
        }
    }

    /**
     * Locates a template suitable for this folder. Eg, enquires
     * to the web.
     *
     * @param name
     * @return
     */
    public ITemplate getTemplate(String name) {
        Web web = getWeb();
        if (web == null) {
            return null;
        }
        TemplateManager tm = _(TemplateManager.class);
        return tm.lookup(name, web);
    }

    @Override
    public String getContentType(String accept) {
        return "httpd/unix-directory";
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        Long l = super.getMaxAgeSeconds(auth);
        return l;
    }

    void onRemoved(BaseResource aThis) {
        log.trace("onRemoved: " + aThis);
    }

    public boolean hasIndexPage() {
        boolean b = (getIndexPage() != null);
        return b;
    }

    public GetableResource getIndexPage() {
        Resource res = child("index.html");
        if (res == null) {
            return null;
        }
        if (res instanceof GetableResource) {
            return (GetableResource) res;
        } else {
            log.debug("  not a GetableResource");
            return null;
        }
    }

    private BaseResource defaultCreateItem(String ct, InputStream in, String newName, Long length) throws ReadingException, WritingException {
        log.trace("defaultCreateItem: " + ct);
        ResourceCreator rc = requestContext().get(ResourceCreator.class);

        // buffer the upload before writing to db
        BufferingOutputStream bufOut = new BufferingOutputStream(100000);
        long bytesWritten = StreamUtils.readTo(in, bufOut, false, true);
        if (bytesWritten != bufOut.getSize()) {
            throw new RuntimeException("Content size mismatch: stream reader reports: " + bytesWritten + " bufOut reports: " + bufOut.getSize());
        }
        if (length != null) {
            if (bytesWritten != length.longValue()) {
                throw new RuntimeException("Content size mismatch: stream reader reports: " + bytesWritten + " content length header: " + length);
            }
        }
        log.trace("uploaded bytes: " + bufOut.getSize());
        in = bufOut.getInputStream();
        BaseResource res = rc.createResource(this, ct, in, newName);
        if (res != null) {
            log.debug("created a: " + res.getClass());
            if (res instanceof BinaryFile) {
                BinaryFile bf = (BinaryFile) res;
                Long actualLength = bf.getContentLength();
                if (actualLength != null && length != null) {
                    if (actualLength.longValue() != length.longValue()) {
                        throw new RuntimeException("Content length mismatch: persisted: " + actualLength + " header: " + length);
                    }
                }
                readItBack(bf, length);
            }
        } else {
            log.debug("resourcecreator returned null");
        }
        return res;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new Folder(parent, newName);
    }

    private void doReplace(Replaceable target, InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        target.replaceContent(in, length);
    }

    @Override
    public String getLink() {
        String text = getLinkText();
        return "<a href='" + getHref() + "index.html'>" + text + "</a>";
    }

    /**
     * If this folder is a thumb folder, or some other type of system folder
     * which users generally arent interested in
     *
     * @return
     */
    public boolean isSystemFolder() {
        // TODO: should check specific conditions, eg parent's thumb specs
        if (getName().startsWith("_sys_")) {
            return true;
        } else if (getName().equals("Recent")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isLockedOutRecursive(Request request) {
        return false; // TODO: lookup from clydelock thingo
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
     * Returns true to indicate that this folder and all child resources, except
     * those which have specified otherwsie, can be versioned if versioning is supported by this installation
     *
     * False means they must not be versioned
     *
     * Null indicates that this folder has not specified a versioning requirement
     * and it has been delegated to its parent (recursively)
     *
     * Note that this property does not return a recursive value, it only returns
     * the value defined on this folder
     *
     * @return - the persisted versioning requirement for this resource
     */
    public Boolean isVersioningEnabled() {
        return versioningEnabled;
    }

    public void setVersioningEnabled(Boolean versioningEnabled) {
        this.versioningEnabled = versioningEnabled;
    }

    @Override
    public boolean isIndexable() {
        return true;
    }

    private void readItBack(BinaryFile bf, long length) {
        InputStream in = bf.getInputStream();
        NullOutputStream nullOut = new NullOutputStream();
        CountingOutputStream cout = new CountingOutputStream(nullOut);
        try {
            IOUtils.copy(in, cout);
        } catch (IOException ex) {
            throw new RuntimeException("io exception when attempting to read back data", ex);
        }
        long persistedSize = cout.getByteCount();
        if (persistedSize != length) {
            throw new RuntimeException("Data integrity failure. Byte sizes do not match: persisted: " + persistedSize);
        }
    }

    /**
     * Required for calendar support. Doesnt really need to be on Folder, could be
     * on Calendar, but should also be on the scheduling collections within
     * calendar so is handy to stick it here
     * 
     * @return 
     */
    public String getCTag() {
        int x = this.hashCode();
        for (Resource r : this.getChildren()) {
            if (r instanceof Folder) {
                Folder tfr = (Folder) r;
                x = x ^ tfr.getCTag().hashCode();
            } else {
                x = x ^ r.getUniqueId().hashCode();
            }
        }
        return "c" + x;
    }

    @Override
    public HrefList getPrincipalCollectionHrefs() {
        HrefList list = new HrefList();
        list.add(getHost().getUsers().getHref());
        return list;
    }

    @FormAction(requiredRole = Role.VIEWER)
    public void bind(@FormParameter(name = "url") String url) {
        Path newPath = Path.path(url);
        Folder destParent = (Folder) getHost().find(newPath.getParent());
        if (destParent == null) {
            throw new RuntimeException("Parent does not exist: " + newPath.getParent());
        }
        LinkedFolder linkedFolder = new LinkedFolder(destParent, newPath.getName());
        linkedFolder.save();
        linkedFolder.setLinkedTo(this);
        commit();
    }

    /**
     * Get this folder's binary CRC, which is a hash of the binary CRC's of folders
     * and binary files inside it, and they're names
     * 
     * This can be used to give an accurate representation of the state of the 
     * binary content of this folder and all of its children
     * 
     * If the value has not been calculated it will return null.
     * 
     * @return 
     */
    public Long getBinaryStateToken() {
        return _(StateTokenManager.class).getStateToken(this);
    }

    /**
     * Alias for getBinaryStateToken so the same property name, crc, can be used
     * for folders and binary files
     * @return 
     */
    public Long getCrc() {
        return getBinaryStateToken();
    }

    public String getBinaryStateTokenData() {
        return _(StateTokenManager.class).getStateTokenData(this);
    }

    /**
     * True if there are, or may be, linked folders which link to this one
     * @return 
     */
    public boolean hasLinkedFolders() {
        return linkedFolders;
    }

    public void setLinkedFolders(boolean linkedFolders) {
        this.linkedFolders = linkedFolders;
    }

    public List<LinkedFolder> getLinkedFolders() {
        return LinkedFolder.getLinkedDestinations(this);
    }

    public Long getCounter() {
        return counter;
    }

    public void setCounter(Long counter) {
        this.counter = counter;
    }
            
    public synchronized Long incrementCounter() {
        if( counter == null ) {
            counter = 0l;
        }
        Long c = counter++;
        save();
        return c;
    }
}
