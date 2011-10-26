package com.ettrema.web.manage.synch;

import java.io.FileOutputStream;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.code.CodeResourceFactory;
import com.ettrema.vfs.VfsSession;
import java.io.File;
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
	private final FileTransport fileTransport;    
    private final ErrorReporter errorReporter;

    public FileLoader(CodeResourceFactory resourceFactory, ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
		this.fileTransport = new DirectFileTransport(resourceFactory);
    }

    public FileLoader(ErrorReporter errorReporter, FileTransport fileTransport) {
        this.errorReporter = errorReporter;
		this.fileTransport = fileTransport;
    }
	
	/**
	 * For backwards compatibility, sets the hostname into the direct file transport
	 * 
	 * @param s 
	 */
	public void	setHostName(String s) {
		if( fileTransport instanceof DirectFileTransport) {
			((DirectFileTransport)fileTransport).setHostName(s);
		} else {
			throw new RuntimeException("Not supported");
		}
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
                    fileTransport.delete(f, root);
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



    public boolean isNewOrUpdated(File f, File root) {
		return fileTransport.isNewOrUpdated(f, root);
    }



    private void upload(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.info("upload: " + f.getAbsolutePath());
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

    }

}
