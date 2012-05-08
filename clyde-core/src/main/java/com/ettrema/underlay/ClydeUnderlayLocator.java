package com.ettrema.underlay;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.Host;
import java.util.List;

import static com.ettrema.context.RequestContext._;
import com.ettrema.web.Folder;

/**
 *
 * @author brad
 */
public class ClydeUnderlayLocator implements UnderlayLocator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeUnderlayLocator.class);
    private String underlayHost;
    private String underlaysFolder = "underlays";

    @Override
    public Host find(UnderlayVector vector) {
        Folder f = getUnderlaysFolder(false);
        if (f == null) {
            log.trace("Could not find underlays folder");
            return null;
        }
        // Now look for version.artifcatId.groupId
        String underlayHostName = vector.getVersion() + "." + vector.getArtifcatId() + "." + vector.getGroupId();
        Resource rUnderlay = f.childRes(underlayHostName);
        if (rUnderlay == null) {
            log.trace("Could not find underlay folder: " + underlayHostName + " in " + f.getHref());
            return null;
        } else if (rUnderlay instanceof Host) {
            return (Host) rUnderlay;
        } else {
            log.warn("Found an underlay folder, but it is not of type Host. Is a: " + rUnderlay.getClass());
            return null;
        }
    }
    
    @Override
    public Host createUnderlay(UnderlayVector vector) {
        Folder f = getUnderlaysFolder(false);
        if (f == null) {
            log.trace("Could not find underlays folder");
            return null;
        }
        
        String underlayHostName = vector.getVersion() + "." + vector.getArtifcatId() + "." + vector.getGroupId();
        if( f.hasChild(underlayHostName)) {
            throw new RuntimeException("Already exists: " + underlayHostName + " in " + f.getHref());
        }
        Host h = new Host(f, underlayHostName);
        h.save();
        return h;
    }

    @Override
    public Folder getUnderlaysFolder(boolean autocreate) {
        Host containerHost = getHost(underlayHost);
        if (containerHost == null) {
            if (autocreate) {
                throw new RuntimeException("Cant autocreate underlays folder because the underlays host does not exist: " + underlayHost);
            }
            return null;
        }

        try {
            Folder f = containerHost.getOrCreateFolder(underlaysFolder);
            if( f.isNew() ) {
                f.save();
            }
            return f;
        } catch (ConflictException | NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Host getHost(String hostName) {
        if (hostName == null) {
            return null;
        }
        List<NameNode> hosts = _(VfsSession.class).find(Host.class, hostName);
        if (hosts == null || hosts.isEmpty()) {
            return null;
        }
        NameNode nnHost = hosts.get(0);
        Resource rHost = (Resource) nnHost.getData();
        if (rHost == null) {
            return null;
        } else if (rHost instanceof Host) {
            Host theHost = (Host) rHost;
            return theHost;
        } else {
            return null;
        }
    }

    public String getUnderlayHost() {
        return underlayHost;
    }

    public void setUnderlayHost(String underlayHost) {
        this.underlayHost = underlayHost;
    }
}
