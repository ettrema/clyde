
package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.security.ClydeAuthenticator;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Subpages defined in templates are inherited from pages which extend the template
 * 
 * When a subpage is located under a page extending its template, the subpage is
 * wrapped in one of these. The WrappedSubPage records the parent which has
 * inherited the subpage and exposes it as the parent
 * 
 * This wrapping page delegates to the wrapped page for most operations. Its behaviour
 * is that of the subpage, but with the logical parent substituted for the physical
 * parent of the subpage
 * 
 * @author brad
 */
public class WrappedSubPage extends CommonTemplated implements PostableResource, ISubPage {
    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( WrappedSubPage.class );
    
    /**The parent that this isntance was found under. Not the physical parent
     * of the subpage
     * 
     */
    final CommonTemplated actualParent;
    final ISubPage subPage;
    
    
    public WrappedSubPage(SubPage subPage, CommonTemplated actualParent) {
        this.subPage = subPage;
        this.actualParent = actualParent;
    }

    public WrappedSubPage(WrappedSubPage subPage, CommonTemplated actualParent) {
        this.subPage = subPage;
        this.actualParent = actualParent;
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        ClydeAuthenticator authenticator = requestContext().get( ClydeAuthenticator.class );
        Object o = authenticator.authenticate( actualParent, digestRequest );
        if( o == null ) {
            log.warn("authentication failed by: " + authenticator.getClass());
        }
        return o;
    }

    @Override
    public User authenticate( String user, String password ) {
        if( log.isTraceEnabled()) {
            log.trace( "authenticate(basic): actualParent: " + actualParent.getHref());
        }
        ClydeAuthenticator authenticator = requestContext().get( ClydeAuthenticator.class );
        User o = authenticator.authenticate( actualParent, user, password );
        if( o == null ) {
            log.warn("authentication failed by: " + authenticator.getClass());
        }
        return o;
    }

    @Override
    public boolean authorise( Request request, Method method, Auth auth ) {
        if( this.subPage.isSecure()) {
            if( auth == null ) {
                log.debug( "authorisation declined, because subpage is secure, and there is no current user");
                return false;
            }
        }
        return actualParent.authorise( request, method, auth );

        // Invitations failed authorisation, because authorisation was delegated
        // to the physical resource which do not allow anonymous access
        //return super.authorise( request, method, auth );
    }



    @Override
    public String getUniqueId() {
        return actualParent.getUniqueId() + subPage.getUniqueId();
    }
    
    
    /**
     * 
     * @return - the physical subpage this is wrapping
     */
    public ISubPage getSubPage() {
        return subPage;
    }

    @Override
    public String getContentType( String accepts ) {
        return subPage.getContentType( accepts );
    }



    /**
     * Recursively looks for the physical page under this and subsequent wrappers
     * 
     * @return
     */
    public SubPage unwrap() {
        if( subPage instanceof WrappedSubPage ) {
            WrappedSubPage next = (WrappedSubPage) subPage;
            return next.unwrap();
        } else {
            return (SubPage) subPage;
        }
    }
    
    @Override
    public Resource getChildResource(String childName) {
        Resource r = null;
        r = subPage.getChildResource(childName);
        if( r == null ) {
            r = super.getChildResource(childName);
        }
        if( r instanceof SubPage ) {
            return new WrappedSubPage((SubPage) r,this);
        } else {
            return r;
        }
    }
    
    /**
     * For backwards compatibility, same as getParent
     * 
     * @return
     */
    public CommonTemplated getFoundParent() {
        return getParent();
    }
    

    @Override
    public Web getWeb() {
        return actualParent.getWeb();
    }

    @Override
    public String getName() {
        return subPage.getName();
    }

    @Override
    public String getRealm() {
        return actualParent.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return subPage.getModifiedDate();
    }

    @Override
    public Path getPath() {
        return actualParent.getPath().child(this.getName());
    }

    @Override
    public ComponentMap getComponents() {
        return subPage.getComponents();
    }

    @Override
    public ComponentValueMap getValues() {
        return subPage.getValues();
    }

    @Override
    public CommonTemplated getParent() {
        return this.actualParent;
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        preProcess(null,parameters,files);
        return process(null,parameters,files);
    }
    
    /** Components should read their values from request params
     */
    @Override
    public void preProcess( RenderContext rcChild,Map<String, String> parameters, Map<String, FileItem> files ) {
        ITemplate lTemplate = getTemplate();
        RenderContext rc = new RenderContext(lTemplate,this,rcChild,false);
        if( lTemplate != null ) {
            lTemplate.preProcess(rc,parameters,files);
            for( ComponentDef def : lTemplate.getComponentDefs().values() ) {
                if( !this.getValues().containsKey(def.getName())) {
                    ComponentValue cv = def.createComponentValue( this );
                    this.getValues().add( cv );
                }
            }
        }

        Collection<Component> all = allComponents();
        for( Component c : all ) {
            c.init(this);
            c.onPreProcess(rc,parameters,files);
        }
    }
    
    /** Commands should be invoked, if user clicked
     */
    @Override
    public String process( RenderContext rcChild,Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        ITemplate lTemplate = getTemplate();
        RenderContext rc = new RenderContext(lTemplate,this,rcChild,false);
        String redirectTo = null;
        if( lTemplate != null ) {
            redirectTo = lTemplate.process(rc,parameters,files);
            if( redirectTo != null ) return redirectTo;
        }
        for( Component c : allComponents() ) {
            redirectTo = c.onProcess(rc,parameters,files);
            if( redirectTo != null ) return redirectTo;
        }
        return null;
    }    

    @Override
    public Collection<Component> allComponents() {
        return allComponents(this);
    }
    
    private BaseResource physicalParent(Templatable parentPage) {
        if( parentPage instanceof BaseResource ) {
            return (BaseResource) parentPage;
        }
        return physicalParent(parentPage.getParent());
    }

    @Override
    public Date getCreateDate() {
        return physicalParent( subPage ).getCreateDate();
    }

    @Override
    public boolean isSecure() {
        return subPage.isSecure();
    }
    
}
