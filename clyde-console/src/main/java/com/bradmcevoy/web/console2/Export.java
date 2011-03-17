package com.bradmcevoy.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.DateUtils.DateParseException;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.XmlWriter;
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
import com.ettrema.console.Result;
import com.ettrema.httpclient.HttpException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author brad
 */
public class Export extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Export.class);
    private com.ettrema.httpclient.Host remoteHost;
    private CodeResourceFactory codeResourceFactory;

    public Export(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, CodeResourceFactory codeResourceFactory) {
        super(args, host, currentDir, resourceFactory);
        this.codeResourceFactory = codeResourceFactory;
    }

    public Result execute() {
        Arguments arguments;
        try {
            arguments = new Arguments(args);
        } catch (Exception ex) {
            log.error("parse", ex);
            return result("Couldnt parse request arguments: " + ex.getMessage());
        }
        try {
            return doImport(arguments);
        } catch (Exception e) {
            log.error("exception in export", e);
            return result(e.getMessage() + arguments.getReport() + "<br/><p style='color: red'>ERRORS OCCURRED!!!</p>");
        }

    }

    private Result doImport(Arguments arguments) throws Exception {
        log.debug("doImport");
        Folder folder = this.currentResource();
        remoteHost = new com.ettrema.httpclient.Host(arguments.destHost, 80, arguments.destUser, arguments.destPassword, null);

        Path destPath = Path.path(arguments.destPath);
        importFolder(folder, arguments, destPath);

        return result("ok: " + arguments.getReport());
    }

    private void importFolder(Folder folder, Arguments arguments, Path destPath) throws Exception {
        log.debug("importFolder: " + folder.getHref());
        for (Resource r : folder.getChildren()) {
            if (r instanceof BaseResource) {
                BaseResource ct = (BaseResource) r;
                if (isImportable(ct, arguments)) {
                    doImport(ct, destPath, arguments);
                    if (arguments.recursive) {
                        if (ct instanceof Folder && !(ct instanceof Host)) {
                            importFolder((Folder) ct, arguments, destPath.child(ct.getName()));
                        }
                    }
                } else {
                    log.debug("not processing: " + ct.getHref());
                }
            }
        }
    }

    private void doImport(BaseResource res, Path path, Arguments arguments) throws Exception {
        log.debug("doImport: " + res.getHref() + " - path:" + path);
        Date localDate = res.getModifiedDate();
        RemoteResource remote = new RemoteResource(path, res);
        Date destDate = remote.getModifiedDate();

        if (isDateApplicalble(destDate, localDate, arguments)) {
            if (arguments.dryRun) {
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

    private boolean isDateApplicalble(Date destDate, Date localDate,  Arguments arguments) {
        log.trace("isDateApplicalble: local: " + localDate + " dest: " + destDate + " since: " + arguments.sinceDate);
        if( arguments.sinceDate != null && localDate.before(arguments.sinceDate)) {
            log.trace(" - not since");
            return false;
        }
        boolean  b= (destDate == null || localDate.after(destDate));
        log.trace(" - " + b);
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
            if (ct instanceof User) {
                if (arguments.noUser) {
                    return false;
                }
            }
            if (ct instanceof Host) {
                return !arguments.stopAtHosts;
            } else {
                if( exportDisabled(ct.getTemplate())) {
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
        if( template == null ) {
            return false;
        } else {
            if( template instanceof Template ) {
                Template t = (Template) template;
                return t.isDisableExport();
            } else {
                return false;
            }
        }
    }

    static class Arguments {

        final String destHost;
        final String destUser;
        final String destPassword;
        final String destPath;
        boolean dryRun;
        boolean recursive;
        boolean stopAtHosts;
        boolean noUser;
        Date sinceDate;
        final List<FileExportStatus> statuses = new ArrayList<FileExportStatus>();

        public Arguments(List<String> args) throws Exception {
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
            } else if( s.startsWith("-since|")) {
                String[] arr = s.split("[|]");
                String sDate = arr[1];
                System.out.println("sincedate:" + sDate);
                sinceDate = DateUtils.parseWebDavDate(sDate);
                log.warn("Ignoring local resources modified before: " + sinceDate);
            }
        }

        String getReport() {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XmlWriter w = new XmlWriter(out);
            w.begin("p").writeText("remote host: " + destHost).close();
            w.begin("p").writeText("remote user: " + destUser).close();
            w.begin("p").writeText("  dryRun: " + dryRun).close();
            w.begin("p").writeText("  recursive: " + recursive).close();
            w.begin("p").writeText("  stop at hosts: " + stopAtHosts).close();

            w.begin("h3").writeText("Uploaded").close();
            XmlWriter.Element elTable = w.begin("table");
            elTable.writeAtt("width", "100%");
            XmlWriter.Element elHeadRow = elTable.begin("tr");
            elHeadRow.begin("th").writeText("local href").close();
            elHeadRow.begin("th").writeText("local mod date").close();
            elHeadRow.begin("th").writeText("remote mod date").close();
            elHeadRow.close();
            for (FileExportStatus fileStat : statuses) {
                if (fileStat.uploaded) {
                    XmlWriter.Element elRow = elTable.begin("tr");
                    elRow.begin("td").writeText(fileStat.localRes.getHref()).close();
                    elRow.begin("td").writeText(formatDate(fileStat.localRes.getModifiedDate())).close();
                    if (fileStat.remoteMod != null) {
                        elRow.begin("td").writeText(formatDate(fileStat.remoteMod)).close();
                    } else {
                        elRow.begin("td").writeText("na").close();
                    }
                    elRow.close();
                }
            }
            elTable.close();

            w.begin("h3").writeText("Skipped").close();
            elTable = w.begin("table");
            elTable.writeAtt("width", "100%");
            elHeadRow = elTable.begin("tr");
            elHeadRow.begin("th").writeText("local href").close();
            elHeadRow.begin("th").writeText("local mod date").close();
            elHeadRow.begin("th").writeText("remote mod date").close();
            elHeadRow.close();
            for (FileExportStatus s : statuses) {
                if (!s.uploaded) {
                    XmlWriter.Element elRow = elTable.begin("tr");
                    elRow.begin("td").writeText(s.localRes.getHref()).close();
                    elRow.begin("td").writeText(formatDate(s.localRes.getModifiedDate())).close();
                    elRow.begin("td").writeText(formatDate(s.remoteMod)).close();
                    elRow.begin("td").writeText(s.comment).close();
                    elRow.close();
                }
            }
            elTable.close();
            w.flush();

            String s = out.toString();
            return s;
        }

        private String formatDate(Date dt) {
            if (dt == null) {
                return "";
            } else {
                return DateUtils.formatDate(dt);
            }
        }

        private void uploaded(XmlPersistableResource r, Date remoteMod) {
            FileExportStatus s = new FileExportStatus(r, remoteMod, true, "");
            statuses.add(s);
        }

        private void skipped(XmlPersistableResource r, Date remoteMod, String reason) {
            FileExportStatus s = new FileExportStatus(r, remoteMod, false, reason);
            statuses.add(s);
        }
    }

    static class FileExportStatus {

        final XmlPersistableResource localRes;
        final Date remoteMod;
        final boolean uploaded;
        final String comment;

        public FileExportStatus(XmlPersistableResource r, Date remoteMod, boolean uploaded, String comment) {
            this.localRes = r;
            this.remoteMod = remoteMod;
            this.uploaded = uploaded;
            this.comment = comment;
        }
    }

    public class RemoteResource {

        Path destFolder;
        BaseResource res;

        public RemoteResource(Path destFolder, BaseResource res) {
            this.destFolder = destFolder;
            this.res = res;
            if( res == null ) {
                throw new IllegalArgumentException("local resource cannot be null");
            }
        }

        /**
         *
         * @return - modified date
         */
        Date getModifiedDate() throws Exception {
            log.debug("doHead: " + destFolder);
            com.ettrema.httpclient.Resource r = remoteHost.find(destFolder + "/" + res.getName());
            if (r == null) {
                log.trace("not found: " + getRemotePath());
                return null;
            }
            return r.getModifiedDate();
        }

        /**
         * Creates or updates the remote resource
         *
         * @throws Exception
         */
        public void doPut() throws Exception {
            log.warn("put: " + this.getRemotePath());
            com.ettrema.httpclient.Folder parent = remoteHost.getOrCreateFolder(destFolder, true);
            com.ettrema.httpclient.Resource remote = parent.child(res.getName());
            try {
                if (remote != null && !(remote instanceof com.ettrema.httpclient.Folder)) {
                    log.warn(" - delete existing remote resource: " + remote.name);
                    remote.delete();
                }
            } catch (Exception e) {
                throw new Exception("Couldnt delete: " + remote.href());
            }
            try {

                updateContentAndMeta(this.destFolder, this.res);


            } catch (Exception ex) {
                throw new Exception("sourceUri:" + res.getHref(), ex);
            }
        }

        private Path getRemotePath() {
            return destFolder.child(res.getName());
        }
    }

    
    private void updateContentAndMeta(Path destFolder, BaseResource res) throws HttpException, IOException, NotAuthorizedException, BadRequestException {
        Path codeFolderPath = Path.path("/_code" + destFolder.toString());
        log.info("export to parent code path: " + codeFolderPath);
        com.ettrema.httpclient.Folder codeParent = remoteHost.getOrCreateFolder(codeFolderPath, true);
        CodeMeta codeMeta = codeResourceFactory.wrapMeta(res, res.getParent());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        codeMeta.sendContent(bytes, null, null, null);
        byte[] arr = bytes.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(arr);
        log.info("upload meta: " + codeMeta.getName() + " - " + codeMeta.getClass());
        System.out.println(bytes.toString());
        codeParent.upload(codeMeta.getName(), bin, arr.length);

        // Now upload content, if not a folder
        if( !(res instanceof Folder) ) {            
            CodeContentPage codeContent = codeResourceFactory.wrapContent(res);
            bytes = new ByteArrayOutputStream();
            codeContent.sendContent(bytes, null, null, null);
            arr = bytes.toByteArray();
            bin = new ByteArrayInputStream(arr);
            log.info("upload content: " + codeContent.getName());
            codeParent.upload(codeContent.getName(), bin, arr.length);
        }
    }
}
