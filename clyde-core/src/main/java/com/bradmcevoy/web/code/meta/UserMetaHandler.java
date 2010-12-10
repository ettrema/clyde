package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import com.bradmcevoy.web.component.InitUtils;
import java.util.Arrays;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class UserMetaHandler implements MetaHandler<User> {

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

    public Iterable<String> getAliases() {
        return Arrays.asList( "user" );
    }

    public Element toXml( User r ) {
        Element elRoot = new Element( "page", CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    public User createFromXml(CollectionResource parent, Element d, String name ) {
        User page = new User((Folder) parent,name);
        updateFromXml(page, d );
        return page;
    }


    private void populateXml( Element el, User page ) {
        InitUtils.setBoolean( el, "emailDisabled", page.isEmailDisabled() );

        Element elEmail = new Element( "email", CodeMeta.NS );
        elEmail.setText( page.getExternalEmailText() );
        el.addContent( elEmail );

        folderMetaHandler.populateXml( el, page );
    }

    public void updateFromXml( User r, Element d ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
