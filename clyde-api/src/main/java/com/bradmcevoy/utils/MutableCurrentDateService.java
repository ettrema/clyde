package com.bradmcevoy.utils;

import java.util.Date;

/**
 * Allows modification of the "current" date for testing purposes
 *
 * @author brad
 */
public class MutableCurrentDateService implements CurrentDateService {

    private Date artificalDate = null;
    private Long dateOffset = null;

	@Override
    public Date getNow() {
        if (artificalDate != null) {
            return artificalDate;
        } else if (dateOffset != null) {
            long tm = System.currentTimeMillis() + dateOffset;
            return new Date(tm);
        } else {
            return new Date();
        }

    }

    public Date getArtificalDate() {
        return artificalDate;
    }

    public void setArtificalDate(Date artificalDate) {
        this.artificalDate = artificalDate;
    }

    public Long getDateOffset() {
        return dateOffset;
    }

    public void setDateOffset(Long dateOffset) {
        this.dateOffset = dateOffset;
    }
    
}
