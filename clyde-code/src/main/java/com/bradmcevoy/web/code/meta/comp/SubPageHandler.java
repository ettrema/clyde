package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.SubPage;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.meta.CommonTemplatedMetaHandler;
import com.bradmcevoy.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class SubPageHandler implements ComponentHandler {

    private static final String ALIAS = "subpage";
    private CommonTemplatedMetaHandler commonTemplatedMetaHandler;

    public SubPageHandler( CommonTemplatedMetaHandler commonTemplatedMetaHandler ) {
        this.commonTemplatedMetaHandler = commonTemplatedMetaHandler;
    }

    public Class getComponentClass() {
        return SubPage.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( Component c ) {
        SubPage t = (SubPage) c;
        Element e2 = new Element( ALIAS, CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public void populateXml( Element e2, SubPage t ) {
        InitUtils.setString( e2, "name", t.getName() );
        if( t.isSecure() ) {
            InitUtils.setBoolean( e2, "secure", t.isSecure() );
        } else {
            if( t.isPublicAccess() ) {
                InitUtils.setBoolean( e2, "public", t.isPublicAccess() );
            }
        }
        InitUtils.set( e2, "redirect", t.getRedirect() );
        InitUtils.set( e2, "browsable", t.isBrowsable() );
        commonTemplatedMetaHandler.populateXml( e2, t, true );
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        SubPage sp = new SubPage( res, name );
        fromXml( sp, el );
        return sp;
    }

    public void fromXml(SubPage sp, Element el) {
        sp.setRedirect(InitUtils.getValue(el, "redirect"));
        updateFromXml(sp, el);
    }

    public void updateFromXml( SubPage sp, Element el ) {
        sp.setSecure( InitUtils.getBoolean( el, "secure" ) );
        sp.setPublicAccess(InitUtils.getBoolean( el, "public" ) );
        sp.setBrowsable(InitUtils.getBoolean( el, "browsable" ) );
        commonTemplatedMetaHandler.updateFromXml( sp, el, true );
    }
}
