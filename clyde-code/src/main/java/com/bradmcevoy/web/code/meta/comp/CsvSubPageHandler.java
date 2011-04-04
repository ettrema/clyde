package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.CsvSubPage;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CsvSubPageHandler implements ComponentHandler {

    private static final String ALIAS = "csvsubpage";
    private SubPageHandler subPageHandler;

    public CsvSubPageHandler( SubPageHandler subPageHandler ) {
        this.subPageHandler = subPageHandler;
    }

    public Class getComponentClass() {
        return CsvSubPage.class;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( Component c ) {
        CsvSubPage t = (CsvSubPage) c;
        Element e2 = new Element( ALIAS, CodeMeta.NS );
        populateXml( e2, t );
        return e2;
    }

    public void populateXml( Element e2, CsvSubPage t ) {
        InitUtils.setString( e2, "name", t.getName() );
        InitUtils.set( e2, "path", t.getSourceFolderPath() );
        InitUtils.set( e2, "selectable", t.getSelectablePath() );
        InitUtils.set( e2, "type", t.getIsType() );
        t.populateFieldsInXml( e2 );
        subPageHandler.populateXml( e2, t );
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        CsvSubPage sp = new CsvSubPage( res, name );
        sp.setSourceFolderPath( InitUtils.getPath( el, "path" ) );
        sp.setIsType( InitUtils.getValue( el, "type" ) );
        sp.setSelectablePath(InitUtils.getPath(el, "selectable"));
        sp.loadFieldsFromXml( el );
        subPageHandler.updateFromXml( sp, el );
        return sp;
    }
}
