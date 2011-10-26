package com.ettrema.migrate;

import com.ettrema.web.BaseResource;
import java.util.Date;

public class FileExportStatus {

    private final BaseResource localRes;
    private final Date remoteMod;
    private final boolean uploaded;
    private final String comment;

    public FileExportStatus(BaseResource r, Date remoteMod, boolean uploaded, String comment) {
        this.localRes = r;
        this.remoteMod = remoteMod;
        this.uploaded = uploaded;
        this.comment = comment;
    }

    public String getLocalId() {
        return localRes.getNameNodeId().toString();
    }

    public String getLocalHref() {
        return localRes.getHref();
    }

    public Date getLocalModDate() {
        return localRes.getModifiedDate();
    }
    
    public Date getRemoteMod() {
        return remoteMod;
    }

    public String getComment() {
        return comment;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    
}
