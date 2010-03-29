package com.bradmcevoy.web;

import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.HtmlDef;
import com.bradmcevoy.web.component.TemplateSelect;

/**
 * This template is pretty bogus. Go with root instead
 * 
 * @author brad
 * @deprecated
 */
@Deprecated
public class RootTemplate extends Template {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RootTemplate.class );
    public static final String HTML_TEMPLATE = ""
        + "<html>\n"
        + "#if($rc.child.editMode)\n"
        + "  $rc.formStart\n"
        + "  View actual page: ${targetPage.link} <br />\n"
        + "  $rc.toolBar\n"
        + "  <br />\n"
        + "#end\n"
        + "$rc.invoke('head',false)\n"
        + "$rc.doBody()\n"
        + "#if($rc.child.editMode)\n"
        + "  $rc.formEnd\n"
        + "#end\n"
        + "</html>";
    private static final long serialVersionUID = 1L;

    public RootTemplate( Folder templates ) {
        super( templates, "rootTemplate.html" );
        TemplateSelect sel = new TemplateSelect( this, "template" );
        this.getComponents().add( sel );
        sel.setValue( "root" );

        ComponentValue cv = new ComponentValue( "body", this );
        cv.init( this );
        cv.setValue( HTML_TEMPLATE );
        this.getValues().add( cv );

        HtmlDef head = new HtmlDef( this, "head" );
        this.getComponentDefs().add( head );
        HtmlDef body = new HtmlDef( this, "body" );
        body.getRows().setValue( 30 );
        body.getCols().setValue( 80 );
        this.getComponentDefs().add( body );
    }
}
