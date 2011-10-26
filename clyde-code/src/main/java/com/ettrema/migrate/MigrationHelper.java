package com.bradmcevoy.migrate;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.XmlPersistableResource;
import com.bradmcevoy.web.code.CodeContentPage;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.CodeResourceFactory;
import com.bradmcevoy.web.recent.RecentResource;
import com.ettrema.httpclient.HttpException;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class MigrationHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MigrationHelper.class);
    private final CodeResourceFactory codeResourceFactory;

    public MigrationHelper(CodeResourceFactory codeResourceFactory) {
        this.codeResourceFactory = codeResourceFactory;
    }

    public void doMigration(Arguments arguments) throws Exception {
        log.trace("doMigration: " + arguments.getDestHost() + ":" + arguments.getDestPort());
        com.ettrema.httpclient.Host remoteHost = new com.ettrema.httpclient.Host(arguments.getDestHost(), arguments.getDestPort(), arguments.getDestUser(), arguments.destPassword(), null);

        // note that destPath is an ADDITIONAL prefix to the path of the resource
        // Eg if resource /b is migrated to a destPath of /a the path to create is /a/b
        Path destPath = Path.path(arguments.getDestPath()); 
        if (arguments.localFolder() != null) {
            log.info("Destination start path (a): " + destPath);
            Path localPath = Path.path(arguments.localFolder().getUrl());
            log.info("Local start path (b): " + localPath);
            destPath = destPath.add(localPath);
            log.info("Destination first resource path (a+b): " + destPath);
            migrateFolder(arguments.localFolder(), arguments, destPath, remoteHost);
        } else {
            migrateIds(arguments.sourceIds(), arguments, destPath, remoteHost);
        }

    }

    private void migrateIds(List<UUID> sourceIds, Arguments arguments, Path destPath, com.ettrema.httpclient.Host remoteHost) throws Exception {
        log.trace("migrateIds: destPath: " + destPath);
        for (UUID id : sourceIds) {
            if (arguments.isFinished()) {
                log.info("cancelled");
                return;
            }
            BaseResource localResource = findLocalResource(id);
            if (localResource == null) {
                log.warn("Couldnt locate resource: " + id);
                arguments.skipped(null, null, "Couldnt locate resource with id: " + id);
            } else {
                Path p = Path.path(localResource.getUrl());                
                Path destParentPath = destPath.add(p).getParent();
                migrateResource(localResource, destParentPath, arguments, remoteHost, true); // force=true because when a file is selected it must be migrated
            }
        }
    }

    private void migrateFolder(Folder folder, Arguments arguments, Path destPath, com.ettrema.httpclient.Host remoteHost) throws Exception {
        log.debug("migrateFolder: " + folder.getHref());
        for (Resource r : folder.getChildren()) {
            if (arguments.isFinished()) {
                log.info("cancelled");
                return;
            }

            if (r instanceof BaseResource) {
                BaseResource ct = (BaseResource) r;
                if (isImportable(ct, arguments)) {
                    migrateResource(ct, destPath, arguments, remoteHost, false);
                    if (arguments.isRecursive()) {
                        if (ct instanceof Folder && !(ct instanceof Host)) {
                            migrateFolder((Folder) ct, arguments, destPath.child(ct.getName()), remoteHost);
                        }
                    }
                } else {
                    log.debug("not processing: " + ct.getHref());
                }
            }
        }
    }

    private void migrateResource(BaseResource res, Path parentPath, Arguments arguments, com.ettrema.httpclient.Host remoteHost, boolean force) throws Exception {
        log.debug("migrateResource: " + res.getHref() + " - path:" + parentPath);
        if (arguments.isFinished()) {
            log.info("cancelled");
            return;
        }

        Date localDate = res.getModifiedDate();
        RemoteResource remote = new RemoteResource(parentPath, res, remoteHost, this);
        Date destDate = remote.getModifiedDate();

        if (isUploadable(remote, res, arguments) || force) {
            if (arguments.isDryRun()) {
                arguments.uploaded(res, destDate);
                return;
            } else {
                try {
                    remote.doPut();
                    arguments.uploaded(res, destDate);
                } catch (Exception e) {
                    log.warn("Failed to put: " + remote.getRemotePath(), e);
                    arguments.skipped(res, destDate, "Upload failed - " + e.getMessage());
                }
            }
        } else {
            arguments.skipped(res, destDate, "Remote file is newer");
            log.debug("not uploading: " + res.getHref() + " because of modified dates: local: " + localDate + " remote:" + destDate);
        }
    }

    private boolean isUploadable(RemoteResource remote, BaseResource res, Arguments arguments) throws Exception {
        Date localDate = res.getModifiedDate();
        Date destDate = remote.getModifiedDate();

        if (isDateApplicalble(destDate, localDate, arguments)) {
            return true;
        } else if (res instanceof Folder) {
            // always upload folder meta-data
            return true;
        } else {
            return false;
        }
    }

    private boolean isDateApplicalble(Date destDate, Date localDate, Arguments arguments) {
        log.trace("isDateApplicalble: local: " + localDate + " dest: " + destDate + " since: " + arguments.getSinceDate());
        if (arguments.getSinceDate() != null && localDate.before(arguments.getSinceDate())) {
            return false;
        }
        boolean b = (destDate == null || localDate.after(destDate));
        return b;
    }

    private boolean isImportable(BaseResource ct, Arguments arguments) {
        if (ct instanceof XmlPersistableResource) {
            if (ct instanceof RecentResource) {
                return false;
            }
            if (ct instanceof BaseResource) {
                BaseResource bres = (BaseResource) ct;
                if (bres.isTrash()) {
                    return false;
                }
            }
            if (ct instanceof Folder) {
                Folder f = (Folder) ct;
                if (f.isSystemFolder()) {
                    return false;
                }
            }
            if (ct instanceof User) {
                if (arguments.isNoUser()) {
                    return false;
                }
            }
            if (ct instanceof Host) {
                return !arguments.isStopAtHosts();
            } else {
                if (exportDisabled(ct.getTemplate()) && !arguments.isForceDisabled()) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return false;
        }
    }

    private boolean exportDisabled(ITemplate template) {
        if (template == null) {
            return false;
        } else {
            if (template instanceof Template) {
                Template t = (Template) template;
                return t.isDisableExport();
            } else {
                return false;
            }
        }
    }

    public void updateContentAndMeta(com.ettrema.httpclient.Host remoteHost, Path destFolder, BaseResource res) throws HttpException, IOException, NotAuthorizedException, BadRequestException {
        Path codeFolderPath = Path.path("/_code" + destFolder.toString());
        log.info("export to parent code path: " + codeFolderPath);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CodeMeta codeMeta = codeResourceFactory.wrapMeta(res, res.getParent());
        if (codeMeta == null) {
            log.info("Not migrating resource: " + res.getHref() + " because code meta factory did not return a resource");
            return;
        }
        codeMeta.sendContent(bytes, null, null, null);
        byte[] arr = bytes.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(arr);

        log.info("upload meta: " + codeMeta.getName() + " - " + codeMeta.getClass());
        Path codeMetaPath = codeFolderPath.child(codeMeta.getName());
        remoteHost.doPut(codeMetaPath, bin, (long) arr.length, "text/xml");

        // Now upload content, if not a folder
        if (!(res instanceof Folder)) {
            CodeContentPage codeContent = codeResourceFactory.wrapContent(res);
            bytes = new ByteArrayOutputStream();
            codeContent.sendContent(bytes, null, null, null);
            arr = bytes.toByteArray();
            bin = new ByteArrayInputStream(arr);
            log.info("upload content: " + codeContent.getName());
            Path codeContentPath = codeFolderPath.child(codeContent.getName());
            remoteHost.doPut(codeContentPath, bin, (long) arr.length, codeContent.getContentType(null));
        }
    }

    private BaseResource findLocalResource(UUID id) {
        VfsSession vfs = _(VfsSession.class);
        NameNode nn = vfs.get(id);
        if (nn == null || nn.getData() == null) {
            log.info("Not found: " + id);
            return null;
        }
        if (nn.getData() instanceof BaseResource) {
            return (BaseResource) nn.getData();
        } else {
            log.warn("not a baseResource, is a: " + nn.getData().getClass());
            return null;
        }
    }
}
