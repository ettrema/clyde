package com.ettrema.web.component;

import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.web.component.Text;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.CommonTemplated.Params;
import com.ettrema.web.Component;
import com.ettrema.web.ComponentMap;
import com.ettrema.web.ComponentValueMap;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.ITemplate;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import com.ettrema.web.Web;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class ComponentUtilsTest {

    MockWeb root;
    MockTemplatable folder;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testHtmlEncode() {

        // Do change when followed by a space
        assertEquals("&amp; ", ComponentUtils.encodeHTML("& "));

        // Do change when not followed by a space
        assertEquals("N&amp;D", ComponentUtils.encodeHTML("N&D"));

        // Don't change numbers
        assertEquals("12", ComponentUtils.encodeHTML("12"));
    }

    @Test
    public void testFindComponentWithRelativePath() {
        root = new MockWeb();
        folder = new MockTemplatable( "folder", root );
        root.children.put( folder.name, folder );
        MockTemplatable page = new MockTemplatable( "page", folder );
        folder.children.put( page.name, page );
        page.getComponentMap().put( "a1", new Text( page, "a1" ) );

        Component c = ComponentUtils.findComponent( Path.path( "a1" ), page );
        assertNotNull( c );
        System.out.println( "-----------------------------" );

        c = ComponentUtils.findComponent( Path.path( "page/a1" ), folder );
        assertNotNull( c );

        System.out.println( "-----------------------------" );

        c = ComponentUtils.findComponent( Path.path( "/folder/page/a1" ), folder );
        assertNotNull( c );

    }

    @Test
    public void testFindContainer() {
    }

    public class MockWeb extends Web {

        private final Map<String, MockTemplatable> children = new HashMap<String, MockTemplatable>();

        public MockWeb(  ) {
            super( null, "root" );
        }

        @Override
        public Resource child( String name ) {
            return children.get( name );
        }
    }

    public class MockTemplatable implements Templatable, CollectionResource {

        private final String name;
        private final Templatable parent;
        private final Map<String, MockTemplatable> children = new HashMap<String, MockTemplatable>();
        private final Map<String, Component> components = new HashMap<String, Component>();

        public MockTemplatable( String name, Templatable parent ) {
            this.name = name;
            this.parent = parent;
        }

        public Map<String, Component> getComponentMap() {
            return components;
        }

        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getTemplateName() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public ITemplate getTemplate() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Collection<Component> allComponents() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Component getComponent( String paramName, boolean includeValues ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean is( String type ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public ComponentValueMap getValues() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public ComponentMap getComponents() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public void preProcess( RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String process( RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getHref() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Web getWeb() {
            return root;
        }

        public Host getHost() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Folder getParentFolder() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Templatable find( Path path ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Templatable getParent() {
            return parent;
        }

        public Params getParams() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getContentType( String accepts ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Date getCreateDate() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getUniqueId() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getName() {
            return name;
        }

        public Object authenticate( String user, String password ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public boolean authorise( Request request, Method method, Auth auth ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String getRealm() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Date getModifiedDate() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public String checkRedirect( Request request ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Addressable getContainer() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Path getPath() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public int compareTo( Resource o ) {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public Component getAnyComponent( String name ) {
            return components.get( name );
        }

        @Override
        public String toString() {
            if( parent != null ) {
                return parent.toString() + "/" + name;
            } else {
                return "/" + name;
            }
        }

        public Resource child( String name ) {
            return children.get( name );
        }

        public List<? extends Resource> getChildren() {
            return new ArrayList<Resource>( children.values() );
        }
    }
}
