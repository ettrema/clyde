package com.ettrema.web.manage.synch;

import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import com.ettrema.logging.LogUtils;
import com.ettrema.vfs.VfsTransactionManager;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author brad
 */
public class FileScanner {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileScanner.class);
    private final RootContext rootContext;
    private final FileLoader fileLoader;

    public FileScanner(RootContext rootContext, FileLoader fileLoader) {
        this.rootContext = rootContext;
        this.fileLoader = fileLoader;
    }

    /**
     * Wraps the entire scan in a single transaction
     *
     * @param forceReload
     * @param root
     * @throws Exception
     */
    public void initialScan(final boolean forceReload, final File root) throws Exception {
        rootContext.execute(new Executable2() {

            @Override
            public void execute(Context context) {
                try {
                    initialScanNoTx(true, root);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
    }

    public void initialScanNoTx(boolean forceReload, File root) throws Exception {
        long t = System.currentTimeMillis();
        log.info("begin full scan");
        VfsTransactionManager.setRollbackOnly(true);
        startScan(root, forceReload);
        VfsTransactionManager.setRollbackOnly(false);
        VfsTransactionManager.commit();
        log.info("------------------------------------");
        log.info("Completed full scan in " + (System.currentTimeMillis() - t) / 1000 + "secs");
        log.info("------------------------------------");
    }

    private void startScan(File root, boolean forceReload) throws Exception {
        log.info("scan files in " + root.getAbsolutePath());
        // BM moved listing.templates check to scanDir so is processed for nested hosts (eg idhealth.com, under 3dn learning)
        scanDir(root, forceReload, root);
    }

    private void scanDir(File dir, boolean forceReload, File root) throws Exception {
        LogUtils.info(log, "scanDir", dir.getAbsolutePath());
        DirectoryListing listing = new DirectoryListing(dir, root);

        processFile(dir, true, root); // force load of dirs for metadata

        // First process the templates folder, if present.
        if (listing.templates != null) {
            scanDir(listing.templates, forceReload, root);
        }
        
        
        for (File f : listing.files) {
            processFile(f, forceReload, root);
        }

        for (File f : listing.subdirs) {
            scanDir(f, forceReload, root);
        }
    }

    private void processFile(final File f, final boolean forceReload, final File root) throws Exception {
        if (forceReload || fileLoader.isNewOrUpdated(f, root)) {
            fileLoader.onNewFile(f, root);
        }
    }

    public boolean isIgnored(File f, File root) {
        return isAnyParentHidden(f, root);
    }

    private boolean isAnyParentHidden(File f, File root) {
        if (f.getName().startsWith(".") && !f.getName().equals(".meta.xml")) {  // special case for file which is meta file of the host, so has no name
            return true;
        } else {
            if (!f.getAbsolutePath().contains(root.getAbsolutePath())) { // reached root
                return false;
            } else {
                return isAnyParentHidden(f.getParentFile(), root);
            }
        }
    }

    private class DirectoryListing {

        File templates;
        final List<File> files = new ArrayList<>();
        final List<File> subdirs = new ArrayList<>();

        public DirectoryListing(File parent, File root) {
            File[] listing = parent.listFiles();
            if (listing != null) {
                List<File> filesList = Arrays.asList(listing);
                Collections.sort(filesList);
                for (File f : filesList) {
                    if (!isIgnored(f, root)) {
                        if (f.isDirectory()) {
                            if ("templates".equals(f.getName())) {
                                this.templates = f;
                            } else {
                                this.subdirs.add(f);
                            }
                        } else {
                            this.files.add(f);
                        }
                    }
                }
            }
        }
    }
}
