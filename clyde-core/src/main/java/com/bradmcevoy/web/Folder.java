package com.bradmcevoy.web;

import com.bradmcevoy.web.creation.ResourceCreator;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.event.EventManager;
import com.bradmcevoy.event.PutEvent;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.io.BufferingOutputStream;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.web.children.ChildFinder;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.TemplateSelect;
import com.bradmcevoy.web.component.Text;
import com.bradmcevoy.web.component.TypeMapping;
import com.bradmcevoy.web.component.TypeMappingsComponent;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.OutputStreamWriter;
import com.ettrema.vfs.RelationalNameNode;
import com.ettrema.vfs.Relationship;
import eu.medsea.util.MimeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdom.Element;
import static com.ettrema.context.RequestContext.*;

/**
 * Represents a folder in the Clyde CMS. Implements collection method interfaces
 * 
 * @author brad
 */
@BeanPropertyResource( "clyde" )
public class Folder extends BaseResource implements com.bradmcevoy.http.FolderResource, XmlPersistableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Folder.class );
    private static final long serialVersionUID = 1L;

    static Folder find( CommonTemplated t ) {
//        log.debug( "find: " + t.getClass() + " - " + t.getName());
        if( t.getParent() == null ) {
            return null;
        }
        if( t.getParent() instanceof Folder ) {
            return (Folder) t.getParent();
        }
        return find( t.getParent() );
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
    private transient List<TransientNameNode> transientNameNodes;

    /** Create a root folder
     */
    public Folder() {
        super();
    }

    public Folder( Folder parentFolder, String newName ) {
        super( null, parentFolder, newName );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        Folder fNew = (Folder) super.copyInstance( parent, newName );
        fNew.secureRead = this.secureRead;
        fNew.secureRead2 = this.secureRead2;
        fNew.versioningEnabled = this.versioningEnabled;
        if( this.templateSpecs != null ) {
            fNew.templateSpecs.addAll( this.templateSpecs );
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

    public void setThumbHref( String thumbHref ) {
        this.thumbHref = thumbHref;
    }

    /**
     * If this folder is not defined as secureRead, recursively check parents until Web
     * 
     * @return
     */
    public boolean isSecureRead() {
        if( log.isTraceEnabled() ) {
            log.trace( "isSecureRead: " + this.getName() + " - " + secureRead );
        }
        if( secureRead  ) {
            return true;
        }
        if( secureRead2 != null ) {
            return secureRead2.booleanValue();
        }
        if( this.getParent() == null ) {
            return false;
        }
        return this.getParent().isSecureRead(); // overridden by Host
    }

    public void setSecureRead( Boolean b ) {
        this.secureRead2 = b;
        if( b != null ) {
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
    public String buildNonDuplicateName( String name ) {
        if( !childExists( name ) ) {
            return name;
        }
        int i = 1;
        while( childExists( name + i ) ) {
            i++;
        }
        return name + i;
    }

    public boolean childExists( String name ) {
        return this.child( name ) != null;
    }

    @Override
    public String getTitle() {
        ComponentValue cv = this.getValues().get( "title" );
        if( cv != null ) {
            Object o = cv.getValue();
            if( o != null ) {
                return o.toString();
            }
        }

        CommonTemplated page = (CommonTemplated) this.getIndexPage();
        if( page != null ) {
            if( !( page instanceof SubPage ) ) { // subpages generally dont add information, they will use the parent folder's info
                return page.getTitle();
            }
        }
        return this.getName();
    }

    @Override
    public Resource getChildResource( String childName ) {
        Resource f = child( childName );
        if( f != null ) {
            return f;
        }

        return super.getChildResource( childName );
    }

    public long getTotalSize() {
        long size = 0;
        for( Resource r : this.getChildren() ) {
            if( r instanceof Folder ) {
                Folder f = (Folder) r;
                size += f.getTotalSize();
            } else {
                if( r instanceof GetableResource ) {
                    GetableResource gr = (GetableResource) r;
                    Long resLength = gr.getContentLength();
                    if( resLength != null ) {
                        size += resLength.longValue();
                    }
                }
            }
        }
        return size;
    }

    public List<Folder> getSubFolders() {
        List<Folder> list = new ArrayList<Folder>();
        for( Resource res : this.getChildren() ) {
            if( res instanceof Folder ) {
                list.add( (Folder) res );
            }
        }
        return list;
    }

    public Folder getSubFolder( String name ) {
        Resource res = this.child( name );
        if( res == null ) {
            log.debug( "sub folder not found: " + name );
            return null;
        } else {
            if( res instanceof Folder ) {
                return (Folder) res;
            } else {
                log.debug( "child is not of type folder. is: " + res.getClass() );
                return null;
            }
        }
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        setSecureRead( InitUtils.getNullableBoolean( el, "secureRead" ) );
        versioningEnabled = InitUtils.getBoolean( el, "versioningEnabled" );
        thumbHref = InitUtils.getValue( el, "thumbHref" );
        String s = el.getAttributeValue( "allowedTemplates" );
        templateSpecs = TemplateSpecs.parse( s );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.set( e2, "secureRead", secureRead2);
        InitUtils.set( e2, "versioningEnabled", versioningEnabled );
        InitUtils.setString( e2, "thumbHref", thumbHref );
        if( templateSpecs == null ) {
            templateSpecs = new TemplateSpecs( "" );
        }
        e2.setAttribute( "allowedTemplates", templateSpecs.format() );

    }

    @Override
    public boolean is( String type ) {
        if( super.is( type ) ) {
            return true;
        } else {
            return type.equals( "folder" );
        }
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        throw new RuntimeException( "Cannot produce content for a folder. Should redirect to index page" );
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
    public void _copyTo( Folder dest, String name ) {
        Folder newFolder = (Folder) copyInstance( dest, name );
        newFolder.templateSelect = (TemplateSelect) newFolder.componentMap.get( "template" );
        newFolder.save();
        log.debug( "created new folder: " + newFolder.getHref() );
        for( Resource child : this.getChildren() ) {
            if( child instanceof BaseResource ) {
                BaseResource resChild = (BaseResource) child;
                log.debug( "copying child: " + resChild.getName() );
                resChild._copyTo( newFolder );
            }
        }
    }

    @Override
    public void onDeleted( NameNode nameNode ) {
        Folder parent = getParent();
        if( parent != null ) {
            parent.onRemoved( this );
        }
    }

    @Override
    public CollectionResource createCollection( String newName ) throws ConflictException {
        log.debug( "createCollection: " + newName + " in folder with id: " + this.getNameNodeId() );
        return createCollection( newName, true );
    }

    public CollectionResource createCollection( String newName, boolean commit ) throws ConflictException {
        Resource res;
        BaseResource child = childRes( newName );
        if( child != null ) {
            throw new RuntimeException( "An item already exists named: " + newName + " - " + child.getClass() + " in " + this.getHref() );
        }
        try {
            res = createNew_notx( newName, null, null, "folder" );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
        if( res instanceof CollectionResource ) {
            if( commit ) {
                commit();
            }
            return (CollectionResource) res;
        } else {
            throw new RuntimeException( "did not create a collection. created a: " + res.getClass().getName() );
        }
    }

    @Override
    public Resource child( String name ) {
        return _( ChildFinder.class ).find( name, this );
    }

    public BaseResource childRes( String name ) {
        //log.trace( "childRes: " + name + " node: " + getNameNodeId() + " this folder: " + this.getName());
        if( getNameNode() == null ) {
            throw new NullPointerException( "nameNode is null" );
        }
        NameNode childNode = getNameNode().child( name );
        if( childNode != null ) {
            DataNode dn = childNode.getData();
            if( dn == null ) {
                return null;
            } else if( dn instanceof BaseResource ) {
                return (BaseResource) dn;
            } else {
                return null;
            }
        } else {
            if( transientNameNodes != null ) {
                for( TransientNameNode nn : transientNameNodes ) {
                    if( nn.getName().equals( name ) ) {
                        DataNode dn = nn.getData();
                        if( dn == null ) {
                            return null;
                        } else if( dn instanceof BaseResource ) {
                            return (BaseResource) dn;
                        } else {
                            return null;
                        }
                    }
                }
            }
            return null;
        }
    }

    public <T> T findFirst( Class<T> c ) {
        for( NameNode n : getNameNode().children() ) {
            if( c.isAssignableFrom( n.getDataClass() ) ) {
                return (T) n.getData();
            }
        }
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() {
        List<Templatable> children = getChildren( null );
        return children;
    }

    public List<Templatable> getChildren( String isA ) {
        return getChildren( isA, null );
    }

    public List<Templatable> getChildren( String isA, String except ) {
        List<Templatable> children = new BaseResourceList();
        for( NameNode n : getNameNode().children() ) {
            DataNode dn = n.getData();
            if( dn != null && dn instanceof BaseResource ) {
                BaseResource res = (BaseResource) dn;
                if( isA == null || res.is( isA ) ) {
                    if( !res.getName().equals( except ) ) {
                        children.add( res );
                    }
                }
            }
        }

        Collections.sort( children );
        return children;
    }

    public List<? extends Resource> getPagesRecursive() {
        List<? extends Resource> list = new BaseResourceList();
        appendChildrenRecursive( list, 0 );
        return list;
    }

    public List<Templatable> children( String template ) {
        return getChildren( template );
    }

    private void appendChildrenRecursive( List list, int depth ) {
        if( depth > 5 ) {
            return;
        }
        for( Resource r : this.getChildren() ) {
            if( r instanceof Folder ) {
                Folder f = (Folder) r;
                if( !f.getName().equals( "templates" ) ) {
                    f.appendChildrenRecursive( list, depth++ );
                }
            } else if( r instanceof Page ) {
                Page p = (Page) r;
                list.add( r );
            } else {
                // do nothing
            }
        }
    }

    @Override
    public Resource createNew( String newName, InputStream in, Long length, String contentType ) throws IOException, ConflictException {
        Resource res = createNew_notx( newName, in, length, contentType );
        EventManager mgr = requestContext().get( EventManager.class );
        if( mgr != null && ( res instanceof BaseResource ) ) {
            mgr.fireEvent( new PutEvent( (BaseResource) res ) );
        }
        commit();
        return res;
    }

    public Resource createNew_notx( String newName, InputStream in, Long length, String contentType ) throws IOException, ConflictException {
        checkHost();
        Resource rExisting = child( newName );
        if( rExisting != null ) {
            if( rExisting instanceof Replaceable ) {
                log.debug( "PUT to a replaceable resource. replacing content..." );
                Replaceable replaceTarget = (Replaceable) rExisting;
                doReplace( replaceTarget, in, length );
                return rExisting;
            } else if( rExisting instanceof BaseResource ) {
                log.debug( "deleting existing item:" + rExisting.getName() );
                ( (BaseResource) rExisting ).delete();
                return doCreate( newName, in, length, contentType );
            } else {
                throw new RuntimeException( "Cannot delete: " + rExisting.getClass().getName() );
            }
        } else {
            log.debug( "creating new item" );
            return doCreate( newName, in, length, contentType );
        }

    }

    private void checkHost() throws ConflictException {
        Host h = this.getHost();
        if( h.isDisabled() ) {
            log.warn( "Attempt to put to a disabled host: " + h.getName() );
            throw new ConflictException( this );
        }
    }

    public Templatable createPage( String name, String template ) {
        ITemplate t = this.getTemplate( template );
        return t.createPageFromTemplate( this, name );
    }

    public Resource doCreate( String newName ) {
        try {
            return doCreate( newName, null, null, null );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Create a resource from a template
     *
     * Does not commit
     * 
     * @param name - the name of the resource to create
     * @param templateName - the name of the template to assign to the resource. Is validated.
     * @return
     */
    public Resource create( String name, String templateName ) {
        ITemplate t = getTemplate( templateName );
        if( t == null ) {
            throw new RuntimeException( "No such template: " + templateName );
        }
        BaseResource res = t.createPageFromTemplate( this, name );
        res.save();
        return res;
    }

    public Resource doCreate( String newName, InputStream in, Long length, String contentType ) throws ReadingException, WritingException {
        log.debug( "doCreate: " + newName + " contentType: " + contentType );
        BaseResource res = null;
        String ct;
        if( contentType == null || contentType.length() == 0 || contentType.equals( "application/octet-stream" ) ) {
            ct = MimeUtil.getMimeType( newName );
        } else {
            ct = contentType;
        }

        List<TypeMapping> typeMappings = getTypeMappings();

        if( typeMappings != null ) {
            for( Path p : ContentTypeUtil.splitContentTypeList( ct ) ) {
                for( TypeMapping tm : typeMappings ) {
                    if( tm.contentType.equals( p.toString() ) ) {
                        ITemplate t = getTemplate( tm.templateName );
                        if( t == null ) {
                            log.warn( "Couldnt find template associated with type mapping: type mapping: " + tm.contentType + " template: " + tm.templateName );
                        } else {
                            log.debug( "found template: " + t.getName() + " from content type: " + tm.contentType );
                            res = t.createPageFromTemplate( this, newName, in, length );
                            res.save();
                            break;
                        }
                    }
                }
            }
        }
        if( res == null ) {
//            log.debug("res was not created through type mappings. falling back to default");
            res = defaultCreateItem( ct, in, newName, length );
        }

        return res;
    }

    public List<TypeMapping> getTypeMappings() {
        Component c = this.getComponent( "typeMappings" );
        List<TypeMapping> typeMappings = null;
        if( c != null ) {
            if( c instanceof TypeMappingsComponent ) {
                typeMappings = ( (TypeMappingsComponent) c ).getValue();
            } else {
                throw new IllegalArgumentException( "typeMappings component must be of type: " + TypeMappingsComponent.class.getName() );
            }
        }
        return typeMappings;
    }

    public Folder thumbs( String thumbSpec ) {
        return thumbs( thumbSpec, false );
    }

    public Folder thumbs( String thumbSpec, boolean create ) {
        String name = thumbSpec + "s";
        Resource res = child( name );
        if( res == null ) {
            if( create ) {
                Folder f = new Folder( this, name );
                f.save();
                return f;
            } else {
                return null;
            }
        } else {
            if( res instanceof Folder ) {
                Folder f = (Folder) res;
                return f;
            } else {
                log.warn( "File of same name as thumbs folder exists: " + name );
                return null;
            }
        }
    }

    /** Called by a child object when it is constructed
     *
     *  Create and return a suitable NameNode
     */
    NameNode onChildCreated( String newName, BaseResource baseResource ) {
//        NameNode nn = nameNode.add(newName,baseResource);
        if( transientNameNodes == null ) {
            transientNameNodes = new ArrayList<TransientNameNode>();
        }
        TransientNameNode nn = new TransientNameNode( newName, baseResource );
        transientNameNodes.add( nn );
        return nn;
    }

    boolean hasChild( String name ) {
        return ( child( name ) != null );
    }

    public TemplateSpecs getTemplateSpecs() {
        return templateSpecs;
    }

    public void setAllowedTemplates( String s ) {
        this.templateSpecs = TemplateSpecs.parse( s );
    }

    public List<Template> getAllowedTemplates() {
        log.debug( "getAllowedTemplates" );
        if( templateSpecs == null || templateSpecs.size() == 0 ) {
            log.debug( "..looking for component" );
            Component c = this.getComponent( "allowedTemplates" );
            if( c != null ) {
                if( c instanceof Text ) {
                    Text t = (Text) c;
                    String s = t.getValue();
                    log.debug( "..got from component: " + s );
                    TemplateSpecs specs = TemplateSpecs.parse( s );
                    List<Template> list = specs.findAllowedDirect( this );
                    return list;
                } else {
                    log.warn( "not a compatible component: " + c.getClass() );
                }
            } else {
                log.debug( "..no component" );
            }
            return getParent().getAllowedTemplates();
        } else {
            return templateSpecs.findAllowed( this );
        }
    }

    /**
     * Locates a template suitable for this folder. Eg, enquires
     * to the web.
     *
     * @param name
     * @return
     */
    public ITemplate getTemplate( String name ) {
        Web web = getWeb();
        if( web == null ) {
            return null;
        }
        TemplateManager tm = _( TemplateManager.class );
        return tm.lookup( name, web );
    }

    @Override
    public String getContentType( String accept ) {
        return "httpd/unix-directory";
    }

    void onRemoved( BaseResource aThis ) {
        log.trace( "onRemoved: " + aThis );
    }

    public boolean hasIndexPage() {
        boolean b = ( getIndexPage() != null );
        return b;
    }

    public GetableResource getIndexPage() {
        Resource res = child( "index.html" );
        if( res == null ) {
            return null;
        }
        if( res instanceof GetableResource ) {
            return (GetableResource) res;
        } else {
            log.debug( "  not a GetableResource" );
            return null;
        }
    }

    private BaseResource defaultCreateItem( String ct, InputStream in, String newName, Long length ) throws ReadingException, WritingException {
        log.trace( "defaultCreateItem: " + ct );
        ResourceCreator rc = requestContext().get( ResourceCreator.class );

        // buffer the upload before writing to db
        BufferingOutputStream bufOut = new BufferingOutputStream( 100000 );
        long bytesWritten = StreamUtils.readTo( in, bufOut, false, true );
        if( bytesWritten != bufOut.getSize() ) {
            throw new RuntimeException( "Content size mismatch: stream reader reports: " + bytesWritten + " bufOut reports: " + bufOut.getSize() );
        }
        if( length != null ) {
            if( bytesWritten != length.longValue() ) {
                throw new RuntimeException( "Content size mismatch: stream reader reports: " + bytesWritten + " content length header: " + length );
            }
        }
        log.trace( "uploaded bytes: " + bufOut.getSize() );
        in = bufOut.getInputStream();
        BaseResource res = rc.createResource( this, ct, in, newName );
        if( res != null ) {
            log.debug( "created a: " + res.getClass() );
            if( res instanceof BinaryFile ) {
                BinaryFile bf = (BinaryFile) res;
                Long actualLength = bf.getContentLength();
                if( actualLength != null && length != null ) {
                    if( actualLength.longValue() != length.longValue() ) {
                        throw new RuntimeException( "Content length mismatch: persisted: " + actualLength + " header: " + length );
                    }
                }
            }
        } else {
            log.debug( "resourcecreator returned null" );
        }
        return res;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Folder( parent, newName );
    }

    private void doReplace( Replaceable target, InputStream in, Long length ) {
        target.replaceContent( in, length );
    }

    @Override
    public String getLink() {
        String text = getLinkText();
        return "<a href='" + getHref() + "index.html'>" + text + "</a>";
    }

    /**
     * TODO: using this as the name node until save is called. better solution then
     *  relying on connections closing to flush transient data
     */
    public class TransientNameNode implements RelationalNameNode {

        final UUID id;
        final String name;
        final DataNode data;
        //final List<Relationship> relations = new ArrayList<Relationship>();
        RelationalNameNode persistedNameNode;

        public TransientNameNode( String name, BaseResource data ) {
            this.id = UUID.randomUUID();
            this.name = name;
            this.data = data;
            data.setId( id );
        }

        @Override
        public InputStream getBinaryContent() {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            return persistedNameNode.getBinaryContent();
        }

        @Override
        public long setBinaryContent( InputStream in ) {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            return persistedNameNode.setBinaryContent( in );
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean hasBinaryContent() {
            if( persistedNameNode == null ) {
                return false;
            }
            return persistedNameNode.hasBinaryContent();
        }

        @Override
        public void setName( String s ) {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            persistedNameNode.setName( s );
        }

        @Override
        public NameNode getParent() {
            return Folder.this.getNameNode();
        }

        @Override
        public NameNode child( String name ) {
            if( persistedNameNode == null ) {
                return null;
            }
            return persistedNameNode.child( name );
        }

        @Override
        public List<NameNode> children() {
            if( persistedNameNode == null ) {
                return null;
            }
            return persistedNameNode.children( true );
        }

        @Override
        public List<NameNode> children( boolean preloadDataNodes ) {
            return children();
        }

        @Override
        public DataNode getData() {
            return data;
        }

        @Override
        public Class getDataClass() {
            return data.getClass();
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public NameNode add( String name, DataNode data ) {
            if( persistedNameNode == null ) {
                save();
            }
            return persistedNameNode.add( name, data );
        }

        @Override
        public void delete() {
            if( persistedNameNode != null ) {
                persistedNameNode.delete();
                persistedNameNode = null;
            }
            transientNameNodes.remove( this );
        }

        @Override
        public void save() {
            persistedNameNode = (RelationalNameNode) getNameNode().add( name, data );
            persistedNameNode.save();
//            ( (BaseResource) data ).nameNode = persistedNameNode;
//            for( Relationship r : relations ) {
//                persistedNameNode.makeRelation( (RelationalNameNode) r.to(), r.relationship() );
//            }
        }

        @Override
        public UUID getParentId() {
            return Folder.this.getNameNodeId();
        }

        @Override
        public Date getCreatedDate() {
            if( persistedNameNode != null ) {
                return persistedNameNode.getCreatedDate();
            }
            return new Date();
        }

        @Override
        public Date getModifiedDate() {
            if( persistedNameNode != null ) {
                return persistedNameNode.getModifiedDate();
            }
            return new Date();
        }

        @Override
        public void onChildNameChanged( String oldName, NameNode childNode ) {
            if( persistedNameNode != null ) {
                persistedNameNode.onChildNameChanged( oldName, childNode );
            }
        }

        @Override
        public void onChildDeleted( NameNode child ) {
            if( persistedNameNode != null ) {
                persistedNameNode.onChildDeleted( child );
            }
        }

        @Override
        public void move( NameNode newParent, String newName ) {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            persistedNameNode.move( newParent, newName );
        }

        @Override
        public void onChildMoved( NameNode child ) {
            if( persistedNameNode != null ) {
                persistedNameNode.onChildMoved( child );
            }
        }

        @Override
        public long writeToBinaryOutputStream( OutputStreamWriter<Long> writer ) {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            return persistedNameNode.writeToBinaryOutputStream( writer );
        }

        @Override
        public Relationship makeRelation( final RelationalNameNode toNode, final String relationshipName ) {
            if( persistedNameNode == null ) {
                this.save();
//                Relationship r = new Relationship() {
//
//                    public NameNode from() {
//                        return getNameNode();
//                    }
//
//                    public NameNode to() {
//                        return toNode;
//                    }
//
//                    public String relationship() {
//                        return relationshipName;
//                    }
//
//                    public void delete() {
//                        relations.remove( this );
//                    }
//                };
//                relations.add( r );
//                log.debug( "makeRelation: no persisted node, use temp: " + relations.size());
//                return r;
//            } else {
//                log.debug( "makeRelation: using persisted node");
//                return persistedNameNode.makeRelation( toNode, relationshipName );
            }
            return persistedNameNode.makeRelation( toNode, relationshipName );
        }

        @Override
        public List<Relationship> findToRelations( String relationshipName ) {
            if( persistedNameNode == null ) {
                return new ArrayList<Relationship>();
            }
            return persistedNameNode.findToRelations( relationshipName );
        }

        @Override
        public List<Relationship> findFromRelations( String relationshipName ) {
            if( persistedNameNode == null ) {
                return Collections.EMPTY_LIST;
//                return relations;
            } else {
                return persistedNameNode.findFromRelations( relationshipName );
            }
        }

        @Override
        public void onNewRelationship( Relationship r ) {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            persistedNameNode.onNewRelationship( r );
        }

        @Override
        public void onDeletedFromRelationship( Relationship r ) {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            persistedNameNode.onDeletedFromRelationship( r );
        }

        @Override
        public void onDeletedToRelationship( Relationship r ) {
            if( persistedNameNode == null ) {
                throw new RuntimeException( "TransientNameNode not yet saved" );
            }
            persistedNameNode.onDeletedToRelationship( r );
        }
    }

    @Override
    public void loadFromXml( Element el, Map<String, String> params ) {
        loadFromXml( el );
    }

    @Override
    public Element toXml( Element el, Map<String, String> params ) {
        return toXml( el );
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

    public void setVersioningEnabled( Boolean versioningEnabled ) {
        this.versioningEnabled = versioningEnabled;
    }

    @Override
    public boolean isIndexable() {
        return true;
    }
}
