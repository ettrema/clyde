package com.ettrema.web.console2;

import com.bradmcevoy.http.DateUtils;
import com.ettrema.migrate.Arguments;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.XmlWriter;
import com.ettrema.migrate.FileExportStatus;
import com.ettrema.migrate.MigrationHelper;
import com.ettrema.web.Folder;
import com.ettrema.console.Result;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

/**
 *
 * @author brad
 */
public class Export extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Export.class);
    private final MigrationHelper migrationHelper;    

    public Export(List<String> args, String host, String currentDir, MigrationHelper migrationHelper, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
        this.migrationHelper = migrationHelper;
    }

    public Result execute() {
        Arguments arguments;
        try {
            Folder folder = this.currentResource();
            arguments = new Arguments(folder, args);
        } catch (Exception ex) {
            log.error("parse", ex);
            return result("Couldnt parse request arguments: " + ex.getMessage());
        }
        try {
            return doImport(arguments);
        } catch (Exception e) {
            log.error("exception in export", e);
            return result(e.getMessage() + getReport(arguments) + "<br/><p style='color: red'>ERRORS OCCURRED!!!</p>");
        }

    }

    private Result doImport(Arguments arguments) throws Exception {
        log.debug("doImport");
        
        migrationHelper.doMigration(arguments);
        return result("ok: " + getReport(arguments));
    }

    public String getReport(Arguments args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter w = new XmlWriter(out);
        w.begin("p").writeText("remote host: " + args.getDestHost()).close();
        w.begin("p").writeText("remote port: " + args.getDestPort()).close();
        w.begin("p").writeText("remote user: " + args.getDestUser()).close();
        w.begin("p").writeText("  dryRun: " + args.isDryRun()).close();
        w.begin("p").writeText("  recursive: " + args.isRecursive()).close();
        w.begin("p").writeText("  stop at hosts: " + args.isStopAtHosts()).close();
        w.begin("h3").writeText("Uploaded").close();
        XmlWriter.Element elTable = w.begin("table");
        elTable.writeAtt("width", "100%");
        XmlWriter.Element elHeadRow = elTable.begin("tr");
        elHeadRow.begin("th").writeText("local href").close();
        elHeadRow.begin("th").writeText("local mod date").close();
        elHeadRow.begin("th").writeText("remote mod date").close();
        elHeadRow.close();
        for (FileExportStatus fileStat : args.getStatuses()) {
            if (fileStat.isUploaded()) {
                XmlWriter.Element elRow = elTable.begin("tr");
                elRow.begin("td").writeText(fileStat.getLocalHref()).close();
                elRow.begin("td").writeText(formatDate(fileStat.getLocalModDate())).close();
                if (fileStat.getRemoteMod() != null) {
                    elRow.begin("td").writeText(formatDate(fileStat.getRemoteMod())).close();
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
        for (FileExportStatus fileStat : args.getStatuses()) {
            if (!fileStat.isUploaded()) {
                XmlWriter.Element elRow = elTable.begin("tr");
                elRow.begin("td").writeText(fileStat.getLocalHref()).close();
                elRow.begin("td").writeText(formatDate(fileStat.getLocalModDate())).close();
                elRow.begin("td").writeText(formatDate(fileStat.getRemoteMod())).close();
                elRow.begin("td").writeText(fileStat.getComment()).close();
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
}
