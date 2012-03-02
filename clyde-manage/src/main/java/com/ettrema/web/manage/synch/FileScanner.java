package com.ettrema.web.manage.synch;

import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import static com.ettrema.context.RequestContext._;
import com.ettrema.context.RootContext;
import com.ettrema.logging.LogUtils;
import com.ettrema.vfs.VfsSession;
import com.ettrema.vfs.VfsTransactionManager;
import java.io.File;
import java.util.ArrayList;
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

    public void initialScan(File root) throws Exception {
        initialScan(false, root);
    }

    public void initialScan(boolean forceReload, File root) throws Exception {
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
        DirectoryListing listing = new DirectoryListing(root, root);
        // First process the templates folder, if present.
        if (listing.templates != null) {
            scanDir(listing.templates, forceReload, root);
        }
        scanDir(root, forceReload, root);
    }

    private void scanDir(File dir, boolean forceReload, File root) throws Exception {
        LogUtils.info(log, "scanDir", dir.getAbsolutePath());
        DirectoryListing listing = new DirectoryListing(dir, root);

        processFile(dir, true, root); // force load of dirs for metadata

        for (File f : listing.files) {
            processFile(f, forceReload, root);
        }

        for (File f : listing.subdirs) {
            scanDir(f, forceReload, root);
        }
    }

    private void processFile(final File f, final boolean forceReload, final File root) throws Exception {
        // If we have a rootContext then execute the operation within a new transaction
        // Otherwise assume we're inside a transaction and just do it
        if (rootContext != null) {
            rootContext.execute(new Executable2() {

                @Override
                public void execute(Context context) {
                    if (forceReload || fileLoader.isNewOrUpdated(f, root)) {
                        try {
                            fileLoader.onNewFile(f, root);
                            VfsTransactionManager.commit();
                        } catch (Exception ex) {
                            VfsTransactionManager.rollback();
                        }
                    }

                }
            });
        } else {
            if (forceReload || fileLoader.isNewOrUpdated(f, root)) {
                fileLoader.onNewFile(f, root);
            }
        }
    }

    public boolean isIgnored(File f, File root) {
        return isAnyParentHidden(f, root);
    }

    private boolean isAnyParentHidden(File f, File root) {
        if (f.getName().startsWith(".")) {
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
                for (File f : listing) {
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
