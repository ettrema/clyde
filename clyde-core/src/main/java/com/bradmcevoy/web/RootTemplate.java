package com.bradmcevoy.web;

import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.HtmlDef;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This template is pretty bogus. Go with root instead
 * 
 * @author brad
 * @deprecated
 */
@Deprecated
public final class RootTemplate extends CommonTemplated implements ITemplate {

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

    private static final Map<Folder,RootTemplate> cache = new HashMap<Folder,RootTemplate>();

    public static synchronized RootTemplate getInstance(Folder templates) {
        RootTemplate r = cache.get(templates);
        if( r == null ) {
            r = new RootTemplate("rootTemplate.html",templates);
            cache.put(templates, r);
        }
        return r;
    }


    private final String name;

    private final ComponentDefMap componentDefs = new ComponentDefMap();

    private final Folder templates;

    private RootTemplate( String name, Folder templates ) {
        this.name = name;
        this.templates = templates;
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

    public Folder createFolderFromTemplate(Folder location, String name) {
        return new Folder(location, name);
    }

    public BaseResource createPageFromTemplate(Folder location, String name, InputStream in, Long length) {
        BaseResource res = createPageFromTemplate(location, name);
        res.save();
        res.setContent(in);
        return res;
    }

    public BaseResource createPageFromTemplate(Folder location, String name) {
        return new Page(location, name);
    }

    @Override
    public ComponentDefMap getComponentDefs() {
        return componentDefs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommonTemplated getParent() {
        return templates;
    }

    @Override
    public String getDefaultContentType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getUniqueId() {
        return null;
    }

    public String getRealm() {
        return templates.getRealm();
    }

    public Date getModifiedDate() {
        return null;
    }

    public Date getCreateDate() {
        return null;
    }

    public ComponentDef getComponentDef(String name) {
        return componentDefs.get(name);
    }

    public boolean represents(String type) {
        return false;
    }

    public boolean canCreateFolder() {
        return true;
    }

    public void onAfterSave(BaseResource aThis) {
        
    }

    public DocType getDocType() {
        return null;
    }

}
