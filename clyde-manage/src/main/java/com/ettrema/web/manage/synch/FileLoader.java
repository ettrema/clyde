package com.ettrema.web.manage.synch;

import java.io.FileOutputStream;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.context.ClassNotInContextException;
import com.ettrema.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author brad
 */
public class FileLoader {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileLoader.class);
    private final FileTransport fileTransport;
    private final ErrorReporter errorReporter;    
    private boolean enableLockWaits = true;


    public FileLoader(ErrorReporter errorReporter, FileTransport fileTransport) {
        this.errorReporter = errorReporter;
        this.fileTransport = fileTransport;
    }

    /**
     * For backwards compatibility of config, sets the hostname into the direct
     * file transport
     *
     * @param s
     */
    public void setHostName(String s) {
        if (fileTransport instanceof DirectFileTransport) {
            ((DirectFileTransport) fileTransport).setHostName(s);
        } else {
            throw new RuntimeException("Not supported");
        }
    }

    public void onNewFile(File f, File root) throws Exception {
        try {
            check(f, root);
        } catch (NotAuthorizedException | ConflictException | BadRequestException | IOException | ClassNotInContextException ex) {
            errorReporter.onError(f, ex);
            throw ex;
        }
    }

    public void onDeleted(File f, File root) throws Exception {
        try {
            check(f, root);
        } catch (NotAuthorizedException | ConflictException | BadRequestException | IOException ex) {
            errorReporter.onError(f, ex);
            throw ex;
        }
    }

    public void onModified(File f, File root) throws Exception {
        try {
            check(f, root);
        } catch (NotAuthorizedException | ConflictException | BadRequestException | IOException ex) {
            errorReporter.onError(f, ex);
            throw ex;
        }
    }

    private void check(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.info("check: " + f.getAbsolutePath() + " exists:" + f.exists());
        if (enableLockWaits) {
            boolean done = false;
            try {
                while (!done) {
                    if (f.exists()) {
                        if (isFileOpen(f)) {
                            log.warn("waiting for file to be unlocked: " + f.getAbsolutePath());
                            Thread.sleep(500);
                        } else {
                            done = true;
                            long t = System.currentTimeMillis();
                            upload(f, root);
                            t = System.currentTimeMillis() - t;
                            log.info("done upload: " + f.getAbsolutePath() + " in " + t / 1000 + "secs");
                        }
                    } else {
                        done = true;
                        fileTransport.delete(f, root);
                        log.info("done delete:" + f.getAbsolutePath());
                    }
                }
            } catch (InterruptedException ex) {
                log.error("interrupted", ex);
            }
        } else {
            if (f.exists()) {
                long t = System.currentTimeMillis();
                upload(f, root);
                t = System.currentTimeMillis() - t;
                log.info("done upload: " + f.getAbsolutePath() + " in " + t / 1000 + "secs");
            } else {
                fileTransport.delete(f, root);
                log.info("done delete:" + f.getAbsolutePath());
            }

        }
    }
    

    private boolean isFileOpen(File file) {
        FileOutputStream fout = null;
        try {
            try {
                fout = new FileOutputStream(file, true);
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

    public boolean isNewOrUpdated(File f, File root) {
        boolean b = fileTransport.isNewOrUpdated(f, root);
        LogUtils.trace(log, "isNewOrUpdated", f.getAbsolutePath(), b);
        return b;
    }

    private void upload(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        try {
            File fMeta = CodeSynchUtils.toMetaFile(f);
            if (fMeta.exists()) {
                fileTransport.put(fMeta, root);
            }
            File fContent = CodeSynchUtils.toContentFile(f);
            if (fContent.exists()) {
                if (fContent.isFile()) {
                    fileTransport.put(fContent, root);
                }
            }
        } catch (Throwable e) {
            log.error("exception loading: " + f.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }   
}
