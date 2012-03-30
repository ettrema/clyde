package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.underlay.UnderlayVector;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class HostMetaHandler implements MetaHandler<Host> {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( HostMetaHandler.class );
    
    public static final String ALIAS = "host";

    private final WebMetaHandler webMetaHandler;

    public HostMetaHandler( WebMetaHandler webMetaHandler ) {
        this.webMetaHandler = webMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return Host.class;
    }

    @Override
    public boolean supports( Resource r ) {
        return r instanceof Host;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }


    @Override
    public Element toXml( Host r ) {
        Element elRoot = new Element( ALIAS, CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    @Override
    public Host createFromXml( CollectionResource parent, Element d, String name ) {
        Folder fParent = (Folder) parent;
        log.info("Creating new host: " + name + " in parent: " + fParent.getHref());
        Host f = new Host( fParent, name );
        updateFromXml( f, d );
        return f;
    }

    @Override
    public void applyOverrideFromXml(Host r, Element el) {
        webMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }
    
    

    public void populateXml( Element el, Host host ) {
        InitUtils.setBoolean( el, "disabled", host.isDisabled() );
        InitUtils.setBoolean( el, "stateTokensDisabled", host.isStateTokensDisabled() );        
        InitUtils.set( el, "aliasedHostPath", host.getAliasedHostPath() );
        
        populateUnderlays(host, el);
        webMetaHandler.populateXml( el, host );
    }

    @Override
    public void updateFromXml( Host host, Element el ) {
        host.setDisabled( InitUtils.getBoolean( el, "disabled" ) );
        host.setStateTokensDisabled( InitUtils.getBoolean( el, "stateTokensDisabled" ) );
        host.setAliasedHostPath( InitUtils.getPath(el, "aliasedHostPath") );
        updateUnderlays(host, el);
        webMetaHandler._updateFromXml( host, el);
        host.save();        
    }

    private void populateUnderlays(Host host, Element el) {
        List<UnderlayVector> underlays = host.getUnderlayVectors();
        if( underlays == null || underlays.isEmpty()) {
            return ;
        }
        Element elUnderlays = new Element("underlays", CodeMeta.NS);
        el.addContent(elUnderlays);
        for( UnderlayVector u : underlays ) {
            Element elUnderlay = new Element("underlay", CodeMeta.NS);
            elUnderlays.addContent(elUnderlay);
            elUnderlay.setAttribute("groupId", u.getGroupId());
            elUnderlay.setAttribute("artifactId", u.getArtifcatId());
            elUnderlay.setAttribute("version", u.getVersion());
        }
        
    }

    private void updateUnderlays(Host host, Element el) {        
        List<UnderlayVector> underlays = null;
        List<Element> elUnderlays = JDomUtils.childrenOf(el, "underlays", CodeMeta.NS);
        log.trace("updateUnderlays: " + elUnderlays.size());
        for( Element elUnderlay : elUnderlays ) {
            if( underlays == null ) {
                underlays = new ArrayList<>();
            }
            UnderlayVector v = new UnderlayVector();
            underlays.add(v);
            v.setGroupId(InitUtils.getValue(elUnderlay, "groupId"));
            v.setArtifcatId(InitUtils.getValue(elUnderlay,"artifactId"));
            v.setVersion(InitUtils.getValue(elUnderlay, "version"));
        }
        host.setUnderlayVectors(underlays);
    }
}
