package com.bradmcevoy.process;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.ReflectionUtils;
import com.bradmcevoy.web.AfterSavable;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.SubPage;
import com.bradmcevoy.web.WrappedSubPage;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import java.util.Map;
import org.jdom.Element;
import org.joda.time.DateTime;

/**
 * This is an implementation of a token which is also a subpage
 * 
 * It is a composition of a SubPage
 * and a Token
 * 
 * @author brad
 */
public class TokenValue extends SubPage implements Token, AfterSavable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TokenValue.class );
    private static final long serialVersionUID = 1L;
    private Token token;
    private ComponentValue cv;

    public TokenValue( Addressable container, Element el ) {
        super( container, el );
        token = TokenUtils.parse( el );
        log.debug( "container: " + container.getClass() + " - " + this.getName());
    }

    public TokenValue( BaseResource newRes, String name, Token t ) {
        super( newRes, name );
        this.token = t;
    }

    /**
     * Set the componentValue that this instance is a composition of
     * 
     * @param cv
     */
    public void setComponentValue( ComponentValue cv ) {
        this.cv = cv;
    }

    ProcessDef getProcess() {
        ITemplate templatePage = this.getParent().getTemplate();
        if( templatePage == null ) {
            return null;
        }
        ComponentDef def = templatePage.getComponentDef( this.getName() );
        if( def == null ) {
            log.warn( "did not find componentdef for: " + this.getName() + " in template: " + templatePage.getName() );
        }
        if( def instanceof ProcessDef ) {
            ProcessDef p = (ProcessDef)def;
            return p;
        } else {
            throw new RuntimeException( "This values Definition is not a processdef");
        }

    }

    @Override
    public String checkRedirect( Request request ) {
        log.debug( "checkRedirect" );
        String stateName = getStateName();
        Resource r = getChildResource( stateName );
        if( r == null ) {
            throw new NullPointerException( "Failed to find child resource for current state: " + stateName );
        } else {
            CommonTemplated ct = (CommonTemplated) r;
            log.debug( "redirecting to: " + ct.getHref() );
            return ct.getHref();
        }
    }

    @Override
    public Resource getChildResource( String childName ) {
        log.debug( "getChildResource: " + childName );
        ProcessDef process = getProcess();
        if( process == null )
            throw new RuntimeException( "processDef not found: " + this.getName() );
        // only return a state if it is the current one
        String currentState = getStateName();
        if( childName.equals( currentState ) ) {
            Resource r = getProcess().getChildResource( childName );
            if( r != null ) return new WrappedSubPage( (SubPage) r, this );
        } else {
            log.debug( "requested state is currently valid. requested: " + childName + "current:" + currentState );
        }
        return super.getChildResource( childName );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        log.debug( "---- toXml");
        Element elThis = super.toXml( container, el );
        populateXml( elThis );
        return elThis;
    }

    public String toXmlString() {
        return token.toXmlString();
    }

    @Override
    public void populateXml( Element el ) {
        token.populateXml( el );
    }

    @Override
    public void setStateName( String name ) {
        token.setStateName( name );
    }

    @Override
    public void setTimeEntered( DateTime dateTime ) {
        token.setTimeEntered( dateTime );
    }

    @Override
    public String getProcessName() {
        return token.getProcessName();
    }

    @Override
    public String getStateName() {
        return token.getStateName();
    }

    @Override
    public Map<String, TokenImpl> getSubTokens() {
        return token.getSubTokens();
    }

    @Override
    public DateTime getTimeEntered() {
        return token.getTimeEntered();
    }

    @Override
    public Map<String, Object> getVariables() {
        return token.getVariables();
    }

    @Override
    public Element toXml() {
        return token.toXml();
    }

    @Override
    public String toString() {
        return token.toXmlString();
    }

    public boolean scan( CommonTemplated creator ) {
        log.debug( "afterSave: creator: " + creator.getHref() + " - " + creator.getClass() );
        BaseResource res = this.getParentFolder();
        ProcessContext context = ProcessDef.createContext( cv, res );
        context.addAttribute( "creator", creator );
        boolean didChange = context.scan();
        if( didChange ) {
            log.debug( "scan made changes, saving resource" );
            res.save();
        }
        return didChange;
    }

    public boolean afterSave() {
        return false;
//        log.debug("afterSave");
//        BaseResource res = (BaseResource) cv.getContainer();
//        return ProcessDef.scan(cv, res);
    }
}
