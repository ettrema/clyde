package com.ettrema.web.code.meta;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.ExistingResourceFactory;
import com.ettrema.web.Folder;
import com.ettrema.web.LinkedFolder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class LinkedFolderMetaHandler implements MetaHandler<LinkedFolder> {

    public static final String ALIAS = "linkedfolder";
    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public LinkedFolderMetaHandler(BaseResourceMetaHandler baseResourceMetaHandler) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return LinkedFolder.class;
    }

    @Override
    public boolean supports(Resource r) {
        return r instanceof LinkedFolder;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public Element toXml(LinkedFolder r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    @Override
    public void applyOverrideFromXml(LinkedFolder r, Element el) {
        baseResourceMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }

    
    @Override
    public LinkedFolder createFromXml(CollectionResource parent, Element d, String name) {
        LinkedFolder f = new LinkedFolder((Folder) parent, name);
        updateFromXml(f, d);
        return f;
    }

    public void populateXml(Element el, LinkedFolder folder) {
        InitUtils.set(el, "linkedTo", folder.getLinkedTo().getPath());
        baseResourceMetaHandler.populateXml(el, folder, true);

    }

    @Override
    public void updateFromXml(LinkedFolder folder, Element d) {
        _updateFromXml(folder, d);
        folder.save();
    }

    public void _updateFromXml(LinkedFolder folder, Element el) {
        Path p = InitUtils.getPath(el, "linkedTo");
        Folder linkedTo;
        try {
            Resource rDest = ExistingResourceFactory.findChild(folder, p);
            if( rDest instanceof Folder ) {
                linkedTo = (Folder) rDest;  
                System.out.println("Linked folder to: " + linkedTo.getHref());
            } else if( rDest == null ) {
                throw new RuntimeException("Could not find path: " + p + " from: " + folder.getHref());
            } else {
                throw new RuntimeException("Found resource: " + p + " from: " + folder.getHref() + " but it is not a regular folder. Is a: " + rDest.getClass());
            }
        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
        folder.setLinkedTo(linkedTo);
        
        baseResourceMetaHandler.updateFromXml(folder, el, false);

    }

}
