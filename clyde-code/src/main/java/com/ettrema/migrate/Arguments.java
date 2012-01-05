package com.ettrema.migrate;

import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.DateUtils.DateParseException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Arguments {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Arguments.class);
    private final Folder localFolder;
    private final List<UUID> ids;
    private final String destHost;
    private final int destPort;
    private final String destUser;
    private final String destPassword;
    private final String destPath;
    private boolean dryRun;
    private boolean recursive;
    private boolean stopAtHosts;
    private boolean noUser;
    private boolean forceDisabled; // force export disabled
    private Date sinceDate;
    private final List<FileExportStatus> statuses = new ArrayList<FileExportStatus>();
    /**
     * Holds a reference to the thread processing this job, if running in background
     */
    private Thread worker;
    boolean finished;

    public Arguments(Folder localFolder, String destHost, int destPort, String destUser, String destPassword, String destPath) {
        this.localFolder = localFolder;
        this.ids = null;
        this.destHost = destHost;
        this.destPort = destPort;
        this.destUser = destUser;
        this.destPassword = destPassword;
        this.destPath = destPath;
    }

    public Arguments(List<UUID> ids, String destHost,int destPort, String destUser, String destPassword, String destPath) {
        this.localFolder = null;
        this.ids = ids;
        this.destHost = destHost;
        this.destPort = destPort;
        this.destUser = destUser;
        this.destPassword = destPassword;
        this.destPath = destPath;
    }

    public Arguments(Folder localFolder, List<String> args) throws Exception {
        this.localFolder = localFolder;
        this.ids = null;
        List<String> list = new ArrayList<String>(args);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String s = it.next();
            if (s.startsWith("-")) {
                processOption(s);
                it.remove();
            } else if (s.trim().length() == 0) {
                it.remove();
            }
        }
        if (list.size() < 3) {
            throw new Exception("not enough arguments");
        }
        String dest = list.get(0);
        log.trace("dest: " + dest);
        URI uriDest = new URI(dest);
        destHost = uriDest.getHost();
        destPath = uriDest.getPath();
        destPort = uriDest.getPort();
        destUser = list.get(1);
        destPassword = list.get(2);
        log.trace("host: " + destHost + " destPath: " + destPassword + " user: " + destUser);
    }

    private void processOption(String s) throws DateParseException {
        if (s.equals("-dry")) {
            dryRun = true;
        } else if (s.equals("-r")) {
            recursive = true;
        } else if (s.equals("-nohost")) {
            stopAtHosts = true;
        } else if (s.equals("-nouser")) {
            noUser = true;
        } else if (s.equals("-force")) {
            forceDisabled = true;
        } else if (s.startsWith("-since|")) {
            String[] arr = s.split("[|]");
            String sDate = arr[1];
            sinceDate = DateUtils.parseWebDavDate(sDate);
            log.warn("Ignoring local resources modified before: " + sinceDate);
        }
    }

    public void uploaded(BaseResource r, Date remoteMod) {
        FileExportStatus s = new FileExportStatus(r, remoteMod, true, "");
        statuses.add(s);
    }

    public void skipped(BaseResource r, Date remoteMod, String reason) {
        FileExportStatus s = new FileExportStatus(r, remoteMod, false, reason);
        statuses.add(s);
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setForceDisabled(boolean forceDisabled) {
        this.forceDisabled = forceDisabled;
    }

    public boolean isForceDisabled() {
        return forceDisabled;
    }

    public void setNoUser(boolean noUser) {
        this.noUser = noUser;
    }

    public boolean isNoUser() {
        return noUser;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setSinceDate(Date sinceDate) {
        this.sinceDate = sinceDate;
    }

    public Date getSinceDate() {
        return sinceDate;
    }

    public void setStopAtHosts(boolean stopAtHosts) {
        this.stopAtHosts = stopAtHosts;
    }

    public boolean isStopAtHosts() {
        return stopAtHosts;
    }

    public String getDestHost() {
        return destHost;
    }

    public int getDestPort() {
        return destPort;
    }        

    public String getDestPath() {
        return destPath;
    }

    public String getDestUser() {
        return destUser;
    }

    public List<FileExportStatus> getStatuses() {
        return statuses;
    }

    public List<UUID> sourceIds() {
        return ids;
    }

    public Integer getNumSourceIds() {
        if (ids == null) {
            return null;
        } else {
            return ids.size();
        }
    }

    public Folder localFolder() {
        return localFolder;
    }

    public void setWorker(Thread thread) {
        this.worker = thread;
    }

    public Thread worker() {
        return worker;
    }

    public void onException(Exception ex) {
        System.out.println("EXCEPTION: ");
        ex.printStackTrace();
        // TODO
    }

    public void onFinished() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    String destPassword() {
        return destPassword;
    }

    public void cancel() {
        finished = true;
        if (worker != null) {
            worker.interrupt();
        }
    }
}
