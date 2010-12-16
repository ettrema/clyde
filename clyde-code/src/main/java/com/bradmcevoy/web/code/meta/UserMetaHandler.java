package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import com.bradmcevoy.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class UserMetaHandler implements MetaHandler<User> {

    public static final String ALIAS = "user";

    private final FolderMetaHandler folderMetaHandler;

    public UserMetaHandler( FolderMetaHandler folderMetaHandler ) {
        this.folderMetaHandler = folderMetaHandler;
    }

    public Class getInstanceType() {
        return User.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof User;
    }

    public String getAlias() {
        return ALIAS;
    }


    public Element toXml( User r ) {
        Element elRoot = new Element( ALIAS, CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    public User createFromXml( CollectionResource parent, Element d, String name ) {
        User page = new User( (Folder) parent, name );
        updateFromXml( page, d );
        return page;
    }

    private void populateXml( Element el, User page ) {
        InitUtils.setBoolean( el, "emailDisabled", page.isEmailDisabled() );

        Element elEmail = new Element( "email", CodeMeta.NS );
        elEmail.setText( page.getExternalEmailText() );
        el.addContent( elEmail );

        folderMetaHandler.populateXml( el, page );
    }

    public void updateFromXml( User user, Element el ) {
        user.setEmailDisabled( InitUtils.getBoolean( el, "emailDisabled" ) );
        String newEmail = JDomUtils.valueOf( el, "email", CodeMeta.NS );
        user.setExternalEmailText( newEmail );
        user.save();
    }
}
