package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.csv.ViewSubPage;
import org.jdom.Element;


/**
 *
 * @author brad
 */
public class ViewSubPageHandler implements ComponentHandler {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ViewSubPageHandler.class );
    private static final String ALIAS = "viewsubpage";
    private SubPageHandler subPageHandler;

    public ViewSubPageHandler( SubPageHandler subPageHandler ) {
        this.subPageHandler = subPageHandler;
    }

    public Class getComponentClass() {
        return ViewSubPage.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( Component c ) {
        log.trace("toXml");
        ViewSubPage t = (ViewSubPage) c;
        Element e2 = new Element( ALIAS, CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public void populateXml( Element e2, ViewSubPage t ) {
        log.trace("populateXml");
        InitUtils.setString( e2, "name", t.getName() );
        InitUtils.set( e2, "path", t.getSourceFolderPath() );
        t.populateFieldsInXml( e2 );
        subPageHandler.populateXml( e2, t );
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        ViewSubPage view = new ViewSubPage( res, name );
        view.setSourceFolderPath( InitUtils.getPath( el, "path" ) );
        view.loadFieldsFromXml( el );
        subPageHandler.updateFromXml( view, el );
        return view;
    }
}
