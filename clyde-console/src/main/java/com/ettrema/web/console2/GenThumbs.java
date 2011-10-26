package com.ettrema.web.console2;

import com.ettrema.media.ThumbGeneratorService;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.vfs.VfsCommon;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.Thumb;
import com.ettrema.console.Result;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContextLocator;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class GenThumbs extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GenThumbs.class);
    private final RootContextLocator rootContextLocator;
    private final int workers;
    private int runningWorkers;
    private final boolean updateWall;

    GenThumbs(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, RootContextLocator rootContextLocator, int workers, boolean updateWall) {
        super(args, host, currentDir, resourceFactory);
        this.rootContextLocator = rootContextLocator;
        this.workers = workers;
        this.updateWall = updateWall;
    }

    @Override
    public Result execute() {
        Resource r = currentResource();
        Folder f = (Folder) r;
        boolean skipIfExists = false;
        List<Thumb> thumbs = null;
        for (String s : args) {
            if (s.equals("-skipIfExists")) {
                skipIfExists = true;
            } else {
                thumbs = Thumb.parseThumbs(s);
            }

        }
        java.util.Queue<Folder> folders = new ArrayBlockingQueue<Folder>(20000);
        long tm = System.currentTimeMillis();
        crawl(f, folders, skipIfExists);
        tm = System.currentTimeMillis() - tm;
        int numFolders = folders.size();
        log.warn("crawled: " + numFolders + " in " + tm / 1000 + " secs");

        for (int i = 0; i < workers; i++) {
            ThumbGenerator gen = new ThumbGenerator(folders, skipIfExists, f.getPath().toString(), thumbs);
            Thread thread = new Thread(gen);
            thread.setDaemon(true);
            thread.start();
        }


        return result("Processing folders: " + numFolders + " running workers: " + runningWorkers);
    }

    private void crawl(Folder f, java.util.Queue<Folder> folders, boolean skipIfExists) {
        log.warn("crawl: " + f.getHref());
        folders.add(f);
        for (Resource r : f.getChildren()) {
            if (r instanceof Folder) {
                Folder fChild = (Folder) r;
                if (!fChild.isSystemFolder()) {
                    crawl(fChild, folders, skipIfExists);
                }
            }
        }
    }

    private boolean isDeprecatedThumbs(Folder fChild) {
        return fChild.getName().equals("regs") || fChild.getName().equals("slideshows") || fChild.getName().equals("thumbs");
    }

    public class ThumbGenerator extends VfsCommon implements Runnable {

        final java.util.Queue<Folder> folders;
        private final boolean skipIfExists;
        private final List<Thumb> thumbs;

        public ThumbGenerator(java.util.Queue<Folder> folders, boolean skipIfExists, String path, List<Thumb> thumbs) {
            this.folders = folders;
            this.skipIfExists = skipIfExists;
            this.thumbs = thumbs;
        }

        public void run() {
            int cnt = 0;
            runningWorkers++;
            try {
                while (!folders.isEmpty()) {
                    final Folder f = folders.remove();
                    final int num = cnt++;
                    log.warn("worker starting new job. Remaining workers: " + runningWorkers + " remaining queue: " + folders.size());
                    rootContextLocator.getRootContext().execute(new Executable2() {

                        public void execute(Context context) {
                            log.warn("processing folder " + num + " of " + folders.size() + " remaining folders");
                            doProcess(context, f.getNameNodeId());
                        }
                    });
                }
            } finally {
                runningWorkers--;
                log.warn("worker completed. Remaining workers: " + runningWorkers + " remaining queue: " + folders.size());
            }
        }

        public void doProcess(Context context, UUID folderId) {
            long tm = System.currentTimeMillis();
            log.warn("starting: " + folderId);
            String name = "unknown - " + folderId;
            int totalThumbs = 0;
            try {
                VfsSession session = context.get(VfsSession.class);
                NameNode nFolder = session.get(folderId);
                if (nFolder == null) {
                    log.error("Name node for host does not exist: " + folderId);
                    return;
                }
                name = nFolder.getName();
                Object data = nFolder.getData();
                if (data == null) {
                    log.error("Data node does not exist. Name node: " + folderId);
                    return;
                }
                if (!(data instanceof Folder)) {
                    log.error("Node does not reference a Folder. Instead references a: " + data.getClass() + " ID:" + folderId);
                    return;
                }

                Folder folder = (Folder) data;
                name = folder.getPath().toString();
                if (isDeprecatedThumbs(folder)) {
                    log.warn("Found deprecated thumbs folder - DELETING: " + folder.getHref());
                    folder.delete();
                } else {
                    log.warn("processing thumbs: " + name + " with thumb specs: " + Thumb.format(thumbs));
                    for (Resource r : folder.getChildren()) {
                        if( r instanceof BaseResource) {
                            _(ThumbGeneratorService.class).doGeneration((BaseResource)r, session);
                        }
                    }
                }
                commit();
            } catch (Exception e) {
                log.error("exception generating thumbs", e);
                rollback();
            } finally {
                tm = System.currentTimeMillis() - tm;
                log.warn("generated: " + totalThumbs + " thumbs in " + tm / 1000 + "secs for: " + name);
            }
        }

        public void pleaseImplementSerializable() {
        }
    }
}
