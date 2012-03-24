package com.ettrema.web.manage.synch;

import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Formatter;
import com.ettrema.web.code.AbstractCodeResource;
import com.ettrema.web.code.CodeFolder;
import com.ettrema.web.code.CodeResourceFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class DirectFileTransport implements FileTransport {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DirectFileTransport.class);
    private final CodeResourceFactory resourceFactory;
    private final FileLoadCallback callback;
    private String hostName = "localhost";

    public DirectFileTransport(String hostName, CodeResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
        this.hostName = hostName;
        this.callback = null;
    }

    public DirectFileTransport(String hostName, CodeResourceFactory resourceFactory, FileLoadCallback callback) {
        this.resourceFactory = resourceFactory;
        this.hostName = hostName;
        this.callback = callback;
    }

    /**
     * Just upload the given file to its parent directory. No name
     * transformations
     *
     * @param f
     */
    @Override
    public void put(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.info("put1: " + f.getAbsolutePath());
        long t = System.currentTimeMillis();
        CodeFolder colParent = findCollection(f.getParentFile(), root);
        if (colParent == null) {
            throw new RuntimeException("Couldnt locate parent: " + f.getParentFile().getAbsolutePath() + " for root: " + root.getAbsolutePath());
        }
        AbstractCodeResource rExisting = (AbstractCodeResource) colParent.child(f.getName());
        FileInputStream fin = null;
        if (rExisting instanceof ReplaceableResource) {
            log.trace("replace content");
            ReplaceableResource replaceable = (ReplaceableResource) rExisting;
            try {
                fin = new FileInputStream(f);
                replaceable.replaceContent(fin, f.length());
                log.info("put2a: replace completed in" + (System.currentTimeMillis()-t) + "ms");
                setSourceModDate(replaceable, f.lastModified());
                if (callback != null) {
                    callback.onModified(replaceable);
                }
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
                    log.info("put2b: about to put " + (System.currentTimeMillis()-t) + "ms");
                    Resource newRes = putable.createNew(f.getName(), fin, f.length(), ct);
                    log.info("put2c: create new completed in" + (System.currentTimeMillis()-t) + "ms");
                    setSourceModDate(newRes, f.lastModified());
                    log.info("put2d: setSourceMod " + (System.currentTimeMillis()-t) + "ms");
                    if (callback != null) {
                        if (rExisting != null) {
                            callback.onModified(newRes);
                        } else {
                            callback.onLoaded(newRes);
                        }
                        log.info("put2e: callbacks " + (System.currentTimeMillis()-t) + "ms");
                    }
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    IOUtils.closeQuietly(fin);
                }
            } else {
                throw new RuntimeException("Can't upload, parent folder doesnt support PUT: " + colParent.getName() + " - " + colParent.getClass());
            }
        }
        log.info("put3: completed in" + (System.currentTimeMillis()-t) + "ms");
    }

    private CodeFolder findCollection(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException {
        String url = CodeSynchUtils.toCodePath(f, root);
        if (url == null) {
            log.warn("Couldnt calculate url for: " + f.getAbsolutePath() + " from root: " + root.getAbsolutePath());
            return null;
        }
        Resource r = resourceFactory.getResource(hostName, url);
        CodeFolder col;
        if (r == null) {
            log.trace("not found: " + url);
            Resource rParent = findCollection(f.getParentFile(), root);
            if (rParent == null) {
                throw new RuntimeException("Couldnt get parent: " + f.getAbsolutePath());
            } else if (rParent instanceof MakeCollectionableResource) {
                MakeCollectionableResource mkcol = (MakeCollectionableResource) rParent;
                col = (CodeFolder) mkcol.createCollection(f.getName());
                return col;
            } else {
                throw new RuntimeException("Cant create " + f.getAbsolutePath() + " parent doesnt support MKCOL");
            }
        } else {
            if (r instanceof CodeFolder) {
                return (CodeFolder) r;
            } else {
                throw new RuntimeException("Found resource but its not a CodeFolder: " + f.getAbsolutePath() + "  maps to a: " + r.getClass());
            }
        }


    }

    @Override
    public void delete(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace("delete: " + f.getAbsolutePath());
        File fMeta = CodeSynchUtils.toMetaFile(f);
        Resource r = resourceFactory.getResource(hostName, CodeSynchUtils.toCodePath(fMeta, root));
        if (r == null) {
            log.trace("not found to delete");
        } else if (r instanceof DeletableResource) {
            DeletableResource dr = (DeletableResource) r;
            dr.delete();
            if (callback != null) {
                callback.onDeleted(dr);
            }
        } else {
            throw new RuntimeException("Cannot delete " + f.getAbsolutePath() + " is a : " + r.getClass());
        }

    }

    @Override
    public boolean isNewOrUpdated(File f, File root) {
        f = CodeSynchUtils.toMetaFile(f);
        Resource r;
        try {
            r = resourceFactory.getResource(hostName, CodeSynchUtils.toCodePath(f, root));
        } catch (NotAuthorizedException | BadRequestException ex) { 
            throw new RuntimeException(ex);
        }
        if (r == null ) {
            return true;
        } else {
            Date sourceMod = getSourceModDate(r); // use this in preference to the modifiedDate of the resource            
            Date mod = sourceMod;
            if( sourceMod == null ) {
                mod = r.getModifiedDate();
            }
            Date fileModDate = new Date(f.lastModified());            
            boolean  b = fileModDate.after(mod);
            LogUtils.trace(log, "isNewOrUpdated", f.getAbsolutePath(), "sourceMod", sourceMod, "resMod", r.getModifiedDate(), "fileMod", fileModDate, "result=", b);
            return b;
        }
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    private void setSourceModDate(Resource newRes, long lastModified) {
        if( newRes instanceof BaseResource) {
            BaseResource r = (BaseResource) newRes;
            Date dt = new Date(lastModified);
            r.setSourceModDate(dt);
            r.save();
            r.commit();
            LogUtils.trace(log, "setSourceModDate", newRes.getName(), dt);
        } else if( newRes instanceof AbstractCodeResource) {
            AbstractCodeResource acr = (AbstractCodeResource) newRes;
            Resource wrapped = acr.getWrapped();
            setSourceModDate(wrapped, lastModified);
        }
    }
    
    private Date getSourceModDate(Resource newRes) {
        if( newRes instanceof BaseResource) {
            BaseResource r = (BaseResource) newRes;
            return r.getSourceModDate();
        } else if( newRes instanceof AbstractCodeResource) {
            AbstractCodeResource acr = (AbstractCodeResource) newRes;
            Resource wrapped = acr.getWrapped();
            return getSourceModDate(wrapped);
        } else {
            return null;
        }  
    }

    public interface FileLoadCallback {

        void onLoaded(Resource replaceable);

        void onDeleted(Resource replaceable);

        public void onModified(Resource replaceable);
    }
}
