package com.bradmcevoy.binary;

import com.bradmcevoy.binary.VersionDescriptor;

/**
 *
 */
public class DefaultVersionDescriptor implements VersionDescriptor {

    private String versionNum;
    private long crc;
    private String userName;
    private long contentLength;

    public String getVersionNum() {
        return versionNum;
    }

    public long getCrc() {
        return crc;
    }

    public String getUserName() {
        return userName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    

}
