package com.ettrema.scheduled;

import com.ettrema.context.Context;
import com.ettrema.grid.Processable;
import java.io.Serializable;

import static com.ettrema.context.RequestContext._;
import com.ettrema.utils.CurrentDateService;
import com.ettrema.utils.LogUtils;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 *
 * @author brad
 */
public class SqlBackupTask implements Processable, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SqlBackupTask.class);
    private static final long serialVersionUID = 1L;
    
    private final CurrentDateService currentDateService;
    
    private String sql;
    
    private File backupDir;
    
    /**
     * If specified, the task will not execute if the hour of day is less then
     * this value
     */
    private Integer notBeforeHour;
    /**
     * If specified, the task will not execute if the hour of day is greater
     * then this value
     */
    private Integer noLaterThenHour;
    /**
     * when the last run occured
     */
    private DateTime lastRun;
    /**
     * The minimum allowable interval, in minutes, between runs
     */
    private int intervalMinutes;
    
    private transient List<String> actions = new ArrayList<>();

    public SqlBackupTask(CurrentDateService currentDateService) {
        this.currentDateService = currentDateService;
    }

    
    
    @Override
    public void doProcess(Context context) {
        if( !isConfigured()) {
            log.trace("Backup task is not configured, so will not perform any backups");
            return ;
        }
        if( !backupDir.exists()) {
            log.info("Backup directory does not exist, will attempt to create: " + backupDir.getAbsolutePath());
            if( !backupDir.mkdirs() ) {
                log.error("Could not create the backup directory, please create it manually: " + backupDir.getAbsolutePath());
                return ;
            }
        }
        Connection con = _(Connection.class);
        if( !isTimeToRun() ) {
            log.trace("doProcess: is not time to run backup");
            return ;
        }
        String sqlToRun = applyTemplates(sql); // TODO parameterise for file name, etc
        LogUtils.info(log, "doProcess: running backup process: " + sqlToRun);
        try {            
            runCommand(con, sqlToRun);
            purgeOldBackups(backupDir);
        } catch (SQLException ex) {
            log.error("Exception running sql: " + sqlToRun, ex);
        }
        
    }
    
    private void runCommand(Connection con, String sqlToRun) throws SQLException {
        try (Statement stmt = con.createStatement() ) {
            stmt.executeQuery(sqlToRun);
        }        
    }    

    public boolean isTimeToRun() {
        Date dtNow = _(CurrentDateService.class).getNow();
        DateTime now = new DateTime(dtNow.getTime());
        if (notBeforeHour != null) {
            if (now.getHourOfDay() < notBeforeHour) {
                log.trace("isTimeToRun: Not time to run, before early hour");
                return false;
            }
        }
        if (noLaterThenHour != null) {
            if (now.getHourOfDay() > noLaterThenHour) {
                log.trace("isTimeToRun: Not time to run, is after late hour");
                return false;
            }
        }
        if (lastRun != null) {
            Duration dur = new Duration(lastRun, now);
            long actualMins = dur.getStandardSeconds() / 60;
            boolean isAfterInterval = actualMins > intervalMinutes;
            LogUtils.trace(log, "isTimeToRun: check is after interval: lastRun", lastRun, "now", now, "intervalMinutes", intervalMinutes, "actual duration", actualMins, isAfterInterval);
            return isAfterInterval;
        }
        log.trace("isTimeToRun: no lastrun and is inside allowed hours, so yes");
        return true;
    }
    
    private void purgeOldBackups(File backupDir) {
        log.trace("purgeOldSnapshots: deleting old backup files");
        SnapshotPurgeContext purgeContext = new SnapshotPurgeContext();
        File[] backups = backupDir.listFiles();
        for( File backup : backups) {
            if (purgeContext.daily.isWithin(backup)) {
                // leave all backups over the last 24 hours
            } else if (purgeContext.weekly.isWithin(backup)) {
                purgeContext.weekly.check(backup);
            } else if (purgeContext.monthly.isWithin(backup)) {
                purgeContext.monthly.check(backup);
            } else if (purgeContext.yearly.isWithin(backup)) {
                purgeContext.yearly.check(backup);
            }
        }

        for (File snapshot : purgeContext.toDelete()) {
            log.debug("deleting snapshot: " + snapshot.getAbsolutePath());
            attemptToDelete(snapshot);
        }
    }

    private void attemptToDelete(File snapshot) {
        if( snapshot.delete()){
            log.info("Deleted: " + snapshot.getAbsolutePath());
        } else {
            log.warn("Unable to delete old backup file: " + snapshot.getAbsolutePath());
        }
    }


    private class SnapshotPurgeContext {

        PurgeInterval daily = new PurgeInterval(24);
        PurgeInterval weekly = new PurgeInterval(24 * 7);
        PurgeInterval monthly = new PurgeInterval(24 * 7 * 4);
        PurgeInterval yearly = new PurgeInterval(24 * 365);

        List<File> toDelete() {
            List<File> list = new ArrayList<>(daily.toDelete);
            list.addAll(weekly.toDelete);
            list.addAll(monthly.toDelete);
            list.addAll(yearly.toDelete);
            return list;
        }
    }

    private class PurgeInterval {

        private final Date intervalStart;
        private File best;
        private List<File> toDelete = new ArrayList<>();

        public PurgeInterval(int hours) {
            this.intervalStart = minusHours(hours);
        }

        /**
         * Check to see if this is the best snapshot to keep. "Best" means most
         * recent, so you must ensure that you only call this for snapshots
         * which are not within a smaller period.
         * 
         * If it is not the best it is added to the toDelete collection
         * 
         * @param snapshot 
         */
        public void check(File snapshot) {
            if (best == null) {
                best = snapshot;
            } else {
                if (snapshot.lastModified() > best.lastModified() ) {
                    toDelete.add(best);
                    best = snapshot;
                }
            }
        }

        /**
         * True if the snapshot was started after this interval started
         * 
         * @param snapshot
         * @return 
         */
        public boolean isWithin(File snapshot) {
            return snapshot.lastModified() > intervalStart.getTime();
        }
    }

    /**
     * Return a date which reprsents the time at the given number of hours ago
     * 
     * @param hours
     * @return 
     */
    private static Date minusHours(int hours) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, hours * -1);
        return cal.getTime();
    }    
    
   
    
    
    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public DateTime getLastRun() {
        return lastRun;
    }

    public Integer getNoLaterThenHour() {
        return noLaterThenHour;
    }

    public void setNoLaterThenHour(Integer noLaterThenHour) {
        this.noLaterThenHour = noLaterThenHour;
    }

    public Integer getNotBeforeHour() {
        return notBeforeHour;
    }

    public void setNotBeforeHour(Integer notBeforeHour) {
        this.notBeforeHour = notBeforeHour;
    }
    

    @Override
    public void pleaseImplementSerializable() {
    }

    private boolean isConfigured() {
        return sql != null && sql.length() > 0;
    }

    private String applyTemplates(String sql) {
        Date now = currentDateService.getNow();
        Calendar calNow = Calendar.getInstance();
        calNow.setTime(now);
        String fname = "backup-" + formatTime() + ".zip";
        File newBackup = new File(backupDir, fname);
        if( newBackup.exists() ) {            
            log.warn("New backup file already exists: " + newBackup.getAbsolutePath() + " will append a big random number...");
            fname = "backup-" + formatTime() + "-" + System.currentTimeMillis() + ".zip";
            newBackup = new File(backupDir, fname);            
        }
        String s = sql.replace("${file}", newBackup.getAbsolutePath());
        return s;
    }

    public File getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(File backupDir) {
        this.backupDir = backupDir;
    }

    private String formatTime() {
        DateFormat sdf = DateFormat.getDateTimeInstance();
        return sdf.format(new Date());
    }

    public List<String> getActions() {
        return actions;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }        
}
