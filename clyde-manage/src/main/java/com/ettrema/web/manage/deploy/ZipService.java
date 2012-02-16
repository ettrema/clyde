package com.ettrema.web.manage.deploy;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class ZipService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZipService.class);

    public File getTempDir() {
        String s = System.getProperty("java.io.tmpdir");
        return new File(s);
    }

    public void unzip(File zipped, File dest) {
        if (!zipped.exists()) {
            throw new RuntimeException("Specified zip file does not exist: " + zipped.getAbsolutePath());
        }
        if (zipped.isDirectory()) {
            throw new RuntimeException("Specified zip file is actually a directory: " + zipped.getAbsolutePath());
        }
        log.info("unzip: " + zipped.getAbsolutePath() + " size: " + zipped.length());
        try {
            ZipFile zipFile = new ZipFile(zipped);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File file = new File(dest, entry.getName());
                if (entry.isDirectory()) {
                    log.trace("unzip: create dir: " + file.getAbsolutePath());
                    if (!file.exists()) {
                        if (!file.mkdirs()) {
                            throw new RuntimeException("Failed to create directory: " + file.getAbsolutePath());
                        }
                    } else {
                        if (!file.isDirectory()) {
                            throw new RuntimeException("Zip directory already exists, but is not a directory: " + file.getAbsolutePath());
                        }
                    }
                } else {
                    log.trace("unzip: extract file: " + file.getAbsolutePath());
                    extractFile(zipFile, entry, file);
                }
            }
        } catch (ZipException ex) {
            throw new RuntimeException("Exception extracting: " + zipped.getAbsolutePath() + " to: " + dest.getAbsolutePath(), ex);
        } catch (IOException ex) {
            throw new RuntimeException("Exception extracting: " + zipped.getAbsolutePath() + " to: " + dest.getAbsolutePath(), ex);
        }

    }

    private void extractFile(ZipFile zipFile, ZipEntry entry, File file) throws IOException {
        try (InputStream in = zipFile.getInputStream(entry)) {
            BufferedInputStream bufIn = new BufferedInputStream(in);
            try (FileOutputStream fout = new FileOutputStream(file)) {
                BufferedOutputStream bufOut = new BufferedOutputStream(fout);
                IOUtils.copyLarge(bufIn, bufOut);
                bufOut.flush();
            }
        }
    }

    public void zip(File sourceDir, File destZip) {
        throw new RuntimeException("not done yet");
    }
}
