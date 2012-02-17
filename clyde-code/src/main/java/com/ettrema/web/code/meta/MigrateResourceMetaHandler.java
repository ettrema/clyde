package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.migrate.MigrateResource;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class MigrateResourceMetaHandler implements MetaHandler<MigrateResource> {

    public static final String ALIAS = "migrator";
    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public MigrateResourceMetaHandler(BaseResourceMetaHandler baseResourceMetaHandler) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    public Class getInstanceType() {
        return MigrateResource.class;
    }

    public boolean supports(Resource r) {
        return r instanceof MigrateResource;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml(MigrateResource r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    public MigrateResource createFromXml(CollectionResource parent, Element d, String name) {
        MigrateResource f = new MigrateResource((Folder) parent, name);
        updateFromXml(f, d);
        return f;
    }

    @Override
    public void applyOverrideFromXml(MigrateResource r, Element el) {
        baseResourceMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }
    
    

    private void populateXml(Element el, MigrateResource page) {
        InitUtils.set(el, "remoteHost", page.getRemoteHost());
        InitUtils.set(el, "remotePath", page.getRemotePath());
        InitUtils.set(el, "remoteUser", page.getRemoteUser());
        InitUtils.set(el, "remotePassword", page.remotePassword());
        InitUtils.set(el, "localPath", page.getLocalPath());
        InitUtils.set(el, "remotePort", page.getRemotePort());
        
        baseResourceMetaHandler.populateXml(el, page);
    }

    public void updateFromXml(MigrateResource r, Element d) {
        baseResourceMetaHandler.updateFromXml(r, d, false);
        
        r.setRemoteHost(InitUtils.getValue(d, "remoteHost"));
        r.setRemotePath(InitUtils.getValue(d, "remotePath"));
        r.setRemoteUser(InitUtils.getValue(d, "remoteUser"));
        r.setRemotePassword(InitUtils.getValue(d, "remotePassword"));
        r.setLocalPath(InitUtils.getPath(d, "localPath"));
        r.setRemotePort(InitUtils.getInt(d, "remotePort"));

        r.save();
    }
}
