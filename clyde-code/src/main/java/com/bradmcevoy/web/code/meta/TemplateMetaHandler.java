package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.ComponentDefMap;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import com.bradmcevoy.web.code.meta.comp.ComponentDefHandler;
import com.bradmcevoy.web.code.meta.comp.DateDefHandler;
import com.bradmcevoy.web.code.meta.comp.EmailDefHandler;
import com.bradmcevoy.web.code.meta.comp.HtmlDefHandler;
import com.bradmcevoy.web.code.meta.comp.TextDefHandler;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.InitUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class TemplateMetaHandler implements MetaHandler<Template> {

    private final PageMetaHandler pageMetaHandler;
    private final Map<Class, String> mapOfAliasesByClass;
    private final Map<String, Class> mapOfClassesByAlias;
    private Map<Class, ComponentDefHandler> mapOfHandlers;
    private Map<String, ComponentDefHandler> mapOfHandlersByAlias;

    public TemplateMetaHandler( PageMetaHandler pageMetaHandler, Map<Class, String> mapOfAliases ) {
        this.pageMetaHandler = pageMetaHandler;
        this.mapOfAliasesByClass = mapOfAliases;
        mapOfClassesByAlias = new HashMap();
        for( Entry<Class, String> entry : mapOfAliasesByClass.entrySet() ) {
            mapOfClassesByAlias.put( entry.getValue(), entry.getKey() );
        }
        mapOfHandlers = new LinkedHashMap<Class, ComponentDefHandler>();
        TextDefHandler textDefHandler = new TextDefHandler();
        HtmlDefHandler htmlDefHandler = new HtmlDefHandler( textDefHandler );
        DateDefHandler dateDefHandler = new DateDefHandler( textDefHandler );
        EmailDefHandler emailDefHandler = new EmailDefHandler( textDefHandler );
        add( emailDefHandler );
        add( dateDefHandler );
        add( htmlDefHandler );
        add( textDefHandler );
    }

    private void add( ComponentDefHandler h ) {
        mapOfHandlers.put( h.getDefClass(), h );
    }

    public Class getInstanceType() {
        return Template.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof Template;
    }

    public Iterable<String> getAliases() {
        return Arrays.asList( "template" );
    }

    public Element toXml( Template r ) {
        Element elRoot = new Element( "template", CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    public Template createFromXml( CollectionResource parent, Element d, String name ) {
        Template page = new Template( (Folder) parent, name );
        updateFromXml( page, d );
        return page;
    }

    private void populateXml( Element el, Template template ) {
        if( template.getAfterCreateScript() != null ) {
            Element elScript = new Element( "afterCreateScript", CodeMeta.NS );
            elScript.setText( template.getAfterCreateScript() );
            el.addContent( elScript );
        }
        String cn = template.getClassToCreate();
        if( !StringUtils.isEmpty( cn ) ) {
            try {
                Class c = Class.forName( cn );
                String s = mapOfAliasesByClass.get( c );
                if( s != null ) cn = s;
            } catch( ClassNotFoundException ex ) {
                Logger.getLogger( TemplateMetaHandler.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }
        InitUtils.set( el, "instanceType", cn );

        initComponentDefs( el, template );

        pageMetaHandler.populateXml( el, template );
    }

    private void initComponentDefs( Element el, Template page ) {
        ComponentDefMap defs = page.getComponentDefs();
        if( defs.isEmpty() ) {
            return;
        }
        Element e2 = new Element( "fields", CodeMeta.NS );
        el.addContent( e2 );
        for( ComponentDef def : defs.values() ) {
            ComponentDefHandler h = mapOfHandlers.get( def.getClass() );
            if( h == null ) {
                throw new RuntimeException( "No ComponentDefHandler for: " + def.getClass() );
            }
            Element elDef = h.toXml( def, page );
            e2.addContent( elDef );
        }
    }

    public void updateFromXml( Template r, Element d ) {
        String instanceType = InitUtils.getValue( d, "instanceType" );
        Class c = mapOfClassesByAlias.get( instanceType );
        r.setClassToCreate( c.getCanonicalName() );

        pageMetaHandler.populateXml( d, r );

        updateDefsFromXml( r, d );

        r.save();
    }

    private void updateDefsFromXml( Template res, Element el ) {
        for( Element eAtt : JDomUtils.childrenOf( el, "fields" ) ) {
            ComponentDefHandler h = mapOfHandlersByAlias.get( eAtt.getName() );
            if( h == null ) {
                throw new RuntimeException( "Couldnt find component handler for element of type: " + eAtt.getName() );
            }
            ComponentDef def = h.fromXml( res, eAtt );
            res.getComponentDefs().add( def );
        }

    }
}
