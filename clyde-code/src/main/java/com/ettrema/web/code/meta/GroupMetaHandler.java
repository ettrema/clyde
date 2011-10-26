package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Group;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class GroupMetaHandler implements MetaHandler<Group> {
    
    public static final String ALIAS = "group";

    private final FolderMetaHandler folderMetaHandler;

    public GroupMetaHandler( FolderMetaHandler folderMetaHandler ) {
        this.folderMetaHandler = folderMetaHandler;
    }

    public Class getInstanceType() {
        return Group.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof Group;
    }

    public String getAlias() {
        return ALIAS;
    }


    public Element toXml( Group r ) {
        Element elRoot = new Element( ALIAS, CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    public Group createFromXml( CollectionResource parent, Element d, String name ) {
        Group f = new Group( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    private void populateXml( Element el, Group page ) {
        InitUtils.setBoolean( el, "emailDisabled", page.isEmailDisabled() );
        InitUtils.setBoolean( el, "secure", page.isSecure() );
        InitUtils.setString( el, "emailPassword", page.getPassword() );
        InitUtils.setString( el, "emailDiscardSubject", page.getEmailDiscardSubjects() );

        folderMetaHandler.populateXml( el, page );
    }

    public void updateFromXml( Group group, Element el ) {
        group.setEmailDisabled( InitUtils.getBoolean( el, "emailDisabled" ) );
        group.setSecureRead( InitUtils.getBoolean( el, "secure" ) );
        group.setPassword( InitUtils.getValue( el, "emailPassword" ) );
        group.setEmailDiscardSubjects( InitUtils.getValue( el, "emailDiscardSubject" ) );
        
        folderMetaHandler._updateFromXml( group, el);

        group.save();
    }
}
