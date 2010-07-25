package com.bradmcevoy.binary;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.web.BinaryFile;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.InputStream;
import java.util.List;

/**
 * BinaryFile's use an instance of this to read and write their binary data
 *
 */
public interface ClydeBinaryService {

    /**
     *
     * @param file
     * @param in
     * @return - the new content length
     */
    int setContent(BinaryFile file, InputStream in);

    /**
     *
     * @param file
     * @param writer
     * @return - the new content length
     */
    int writeToOutputStream(BinaryFile file, final OutputStreamWriter<Long> writer);

    /**
     *
     * @param file
     * @param versionNum
     * @return
     * @throws BadRequestException - if the version couldnt be found
     */
    InputStream readInputStream(BinaryFile file, String versionNum) throws BadRequestException;

    long getContentLength(BinaryFile file, String versionNum);

    long getCrc(BinaryFile file, String versionNum);

    List<VersionDescriptor> getVersions(BinaryFile file);
}
