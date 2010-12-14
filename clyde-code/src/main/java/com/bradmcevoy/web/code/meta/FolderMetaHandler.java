package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.TemplateSpecs;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import com.bradmcevoy.web.component.InitUtils;
import java.util.Arrays;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class FolderMetaHandler implements MetaHandler<Folder> {

    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public FolderMetaHandler( BaseResourceMetaHandler baseResourceMetaHandler ) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    public Class getInstanceType() {
        return Folder.class;
    }


    public boolean supports( Resource r ) {
        return r instanceof Folder;
    }

    public Iterable<String> getAliases() {
        return Arrays.asList( "folder" );
    }

    public Element toXml( Folder r ) {
        Element elRoot = new Element( "folder", CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    public Folder createFromXml(CollectionResource parent, Element d, String name ) {
        Folder f = new Folder( (Folder) parent,name);
        updateFromXml(f, d );
        return f;
    }



    public void populateXml( Element elRoot, Folder folder ) {
        InitUtils.set( elRoot, "secureRead", folder.isSecureRead2() );
        InitUtils.set( elRoot, "versioningEnabled", folder.isVersioningEnabled() );
        TemplateSpecs templateSpecs = folder.getTemplateSpecs();
        if( templateSpecs == null ) {
            templateSpecs = new TemplateSpecs( "" );
        }
        InitUtils.set( elRoot, "allowedTemplates", templateSpecs.format());
        baseResourceMetaHandler.populateXml( elRoot, folder );

    }

    public void updateFromXml( Folder folder, Element d ) {
        _updateFromXml( folder, d );
        folder.save();
    }

    public void _updateFromXml( Folder folder, Element d ) {
        folder.setSecureRead( InitUtils.getBoolean( d, "secureRead"));
        folder.setVersioningEnabled( InitUtils.getBoolean( d, "versioningEnabled"));
        String sAllowedTemplates = d.getAttributeValue( "allowedTemplates" );
        TemplateSpecs templateSpecs = TemplateSpecs.parse( sAllowedTemplates );
        folder.setTemplateSpecs( templateSpecs );
          
        baseResourceMetaHandler.updateFromXml( folder, d, false);
        
    }
}
