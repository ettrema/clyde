package com.ettrema.patches;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.RootFolder;
import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.ExistingResourceFactory;
import java.util.List;

/**
 * Bean intended to be configured into spring context. It will check for the
 * presence of the named host. If not found it will create it as a root host.
 *
 * @author brad
 */
public class RootHostCreator implements Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RootHostCreator.class);
    private final RootContext rootContext;
    private final ExistingResourceFactory resourceFactory;
    private String hostName;

    public RootHostCreator(RootContext rootContext, ExistingResourceFactory resourceFactory, String hostName) {
        this.rootContext = rootContext;
        this.resourceFactory = resourceFactory;
        this.hostName = hostName;
    }

    private void checkAndCreate(Context context) {
        VfsSession sess = context.get(VfsSession.class);
        if (sess == null) {
            throw new RuntimeException("No VfsSession could be obtained. Check configuration");
        }

        List<NameNode> list = sess.find(Host.class, hostName);
        if (list != null && list.size() > 0) {
            log.debug("Found existing host " + hostName + " with id: " + list.get(0).getId());
            if (list.size() > 0) {
                log.warn("Found multiple hosts with the same name: " + hostName);
            }
            return;
        }

        log.warn("host not found, creating: " + hostName);
        Folder rootFolder = findOrCreateRootFolder(sess);
        Host rootHost = new Host(rootFolder, hostName);
        rootHost.save();
        sess.commit();
        log.info("Created new host and committed");
    }

    private void checkAndCreate() {
        rootContext.execute(new Executable2() {

            @Override
            public void execute(Context context) {
                try {
                    checkAndCreate(context);
                } catch (Exception e) {
                    log.error("Exception occured checked root host, will continue anyway...", e);
                }
            }
        });

        // Now confirm is all working
        rootContext.execute(new Executable2() {

            @Override
            public void execute(Context context) {
                try {
                    Resource r = resourceFactory.getResource(hostName, "/");
                    if (r == null) {
                        log.warn("------------ Couldnt locate host -------------");
                    } else if( r instanceof Host) {
                        log.info("Confirmed root host exists: " + r.getName());
                    } else {
                        log.warn("------------ Root host is not correct class: " + r.getClass() + " -------------");
                    }
                } catch (NotAuthorizedException | BadRequestException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

    }

    private Folder findOrCreateRootFolder(VfsSession sess) {
        NameNode nn = sess.find(Path.path("/root"));
        if (nn == null) {
            RootFolder rootFolder = new RootFolder(sess.root());
            rootFolder.save();
            return rootFolder;
        } else {
            DataNode data = nn.getData();
            if (data == null) {
                throw new RuntimeException("RootFolder node contains a null datanode: " + nn.getId());
            }
            if (data instanceof RootFolder) {
                return (RootFolder) data;
            } else {
                log.warn("root is not a RootFolder: " + nn.getId());
                if (data instanceof Folder) {
                    return (Folder) data;
                } else {
                    throw new RuntimeException("Node at root folder location /root is not a folder. Is a: " + data.getClass());
                }
            }
        }
    }

    @Override
    public void start() {
        checkAndCreate();
    }

    @Override
    public void stop() {
    }
}
