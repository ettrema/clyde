package com.bradmcevoy.web.manage.synch;

import java.util.Date;
import java.io.FileOutputStream;
import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.code.CodeResourceFactory;
import com.ettrema.vfs.VfsSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class FileLoader {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileLoader.class);
    private String hostName = "test.ppod.com";  // todo: fix config to use localhost
    //private String hostName = "127.0.0.1";
    private final CodeResourceFactory resourceFactory;
    private final ErrorReporter errorReporter;

    public FileLoader(CodeResourceFactory resourceFactory, ErrorReporter errorReporter) {
        this.resourceFactory = resourceFactory;
        this.errorReporter = errorReporter;
    }

    public void onNewFile(File f, File root) {
        try {
            check(f, root);
            log.trace("commit new file");
            _(VfsSession.class).commit();
        } catch (Exception ex) {
            _(VfsSession.class).rollback();
            errorReporter.onError(f, ex);
        }
    }

    public void onDeleted(File f, File root) {
        try {
            check(f, root);
            log.trace("commit deleted file");
            _(VfsSession.class).commit();
        } catch (Exception ex) {
            _(VfsSession.class).rollback();
            errorReporter.onError(f, ex);
        }
    }

    public void onModified(File f,File root) {
        try {
            check(f, root);
            log.trace("commit modified file");
            _(VfsSession.class).commit();
        } catch (Exception ex) {
            _(VfsSession.class).rollback();
            errorReporter.onError(f, ex);
        }
    }

    private void check(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        boolean done = false;
        try {
            while (!done) {
                Thread.sleep(300);
                if (f.exists()) {
                    if (isFileOpen(f)) {
                        log.warn("waiting for file to be unlocked: " + f.getAbsolutePath());
                        Thread.sleep(1000);
                    } else {
                        done = true;
                        long t = System.currentTimeMillis();
                        upload(f, root);
                        t = System.currentTimeMillis() - t;
                        log.info("done upload: " + f.getAbsolutePath() + " in " + t/1000 + "secs");
                    }
                } else {
                    done = true;
                    delete(f, root);
                    log.info("done delete:" + f.getAbsolutePath());
                }
            }
        } catch (InterruptedException ex) {
            log.error("interrupted", ex);
        }

    }

    private boolean isFileOpen(File file) {
        FileOutputStream fout = null;
        try {
            try {
                fout = new FileOutputStream(file, true);
                log.trace("not locked");
                return false;
            } catch (FileNotFoundException ex) {
                log.info("file doesnt exist: " + file.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            log.info("exception occured, so presume file is locked: " + file.getAbsolutePath() + " - " + e.getMessage());
            return true;
        } finally {
            IOUtils.closeQuietly(fout);
        }
    }

    public void onRenamed(File f) {
    }

    public boolean exists(File f, File root) {
        f = toMetaFile(f);
        Resource r = resourceFactory.getResource(hostName, toUrl(f, root));
        return r != null;
    }

    public boolean isNewOrUpdated(File f, File root) {
        f = toMetaFile(f);
        Resource r = resourceFactory.getResource(hostName, toUrl(f, root));
        if (r == null || r.getModifiedDate() == null) {
            return true;
        } else {
            Date fileModDate = new Date(f.lastModified());
            return fileModDate.after(r.getModifiedDate());
        }
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public CodeResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    private File toContentFile(File f) {
        if (f.getName().endsWith(".meta.xml")) {
            String contentName = f.getName().replace(".meta.xml", "");
            return new File(f.getParentFile(), contentName);
        } else {
            return f;
        }
    }

    private File toMetaFile(File f) {
        if (f.getName().endsWith(".meta.xml")) {
            return f;
        } else {
            String metaName = f.getName() + ".meta.xml";
            return new File(f.getParentFile(), metaName);

        }
    }

    private String toUrl(File f, File root) {
        String s = f.getAbsolutePath();
        if (s.startsWith(root.getAbsolutePath())) {
            s = s.replace(root.getAbsolutePath(), "");
            s = s.replace("\\", "/");
            s = "/_code" + s;
            return s;
        } else {
            log.warn("File does not begin with the root path. File:" + f.getAbsolutePath() + " root: " + root.getAbsolutePath());
            return null;
        }
    }

    private void upload(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.info("upload: " + f.getAbsolutePath());
        File fMeta = toMetaFile(f);
        if (fMeta.exists()) {
            put(fMeta, root);
        }
        File fContent = toContentFile(f);
        if (fContent.exists()) {
            if (fContent.isFile()) {
                put(fContent, root);
            }
        }

    }

    /**
     * Just upload the given file to its parent directory.
     * No name transformations
     * @param f
     */
    private void put(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.trace("put: " + f.getAbsolutePath());
        CollectionResource colParent = findCollection(f.getParentFile(), root);
        if( colParent == null ) {
            throw new RuntimeException("Couldnt locate parent: " + f.getParentFile().getAbsolutePath() + " for root: " + root.getAbsolutePath());
        }
        Resource rExisting = colParent.child(f.getName());
        FileInputStream fin = null;
        if (rExisting instanceof ReplaceableResource) {
            log.trace("replace content");
            ReplaceableResource replaceable = (ReplaceableResource) rExisting;
            try {
                fin = new FileInputStream(f);
                replaceable.replaceContent(fin, f.length());
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } finally {
                IOUtils.closeQuietly(fin);
            }
        } else {
            if (rExisting != null) {
                log.trace("resource found, but is not replaceable. create new resource: " + rExisting.getClass());
            } else {
                log.trace("no resource found, create new resource");
            }
            if (colParent instanceof PutableResource) {
                PutableResource putable = (PutableResource) colParent;
                try {
                    fin = new FileInputStream(f);
                    String ct = ContentTypeUtils.findContentTypes(f);
                    putable.createNew(f.getName(), fin, f.length(), ct);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    IOUtils.closeQuietly(fin);
                }
            } else {
                throw new RuntimeException("Can't upload, parent folder doesnt support PUT: " + colParent.getName() + " - " + colParent.getClass());
            }
        }
    }

    private CollectionResource findCollection(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException {
        String url = toUrl(f, root);
        if (url == null) {
            log.warn("Couldnt calculate url for: " + f.getAbsolutePath() + " from root: " + root.getAbsolutePath());
            return null;
        }
        Resource r = resourceFactory.getResource(hostName, url);
        CollectionResource col;
        if (r == null) {
            log.trace("not found: " + url);
            Resource rParent = findCollection(f.getParentFile(), root);
            if (rParent == null) {
                throw new RuntimeException("Couldnt get parent: " + f.getAbsolutePath());
            } else if (rParent instanceof MakeCollectionableResource) {
                MakeCollectionableResource mkcol = (MakeCollectionableResource) rParent;
                col = mkcol.createCollection(f.getName());
                return col;
            } else {
                throw new RuntimeException("Cant create " + f.getAbsolutePath() + " parent doesnt support MKCOL");
            }
        } else {
            if (r instanceof CollectionResource) {
                return (CollectionResource) r;
            } else {
                throw new RuntimeException("Found resource but its not a collection: " + f.getAbsolutePath());
            }
        }


    }

    private void delete(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace("delete: " + f.getAbsolutePath());
        File fMeta = toMetaFile(f);
        Resource r = resourceFactory.getResource(hostName, toUrl(fMeta, root));
        if (r == null) {
            log.trace("not found to delete");
        } else if (r instanceof DeletableResource) {
            DeletableResource dr = (DeletableResource) r;
            dr.delete();
        } else {
            throw new RuntimeException("Cannot delete " + f.getAbsolutePath() + " is a : " + r.getClass());
        }

    }
}
