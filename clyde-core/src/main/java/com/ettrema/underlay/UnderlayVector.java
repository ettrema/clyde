package com.ettrema.underlay;

import java.io.Serializable;

/**
 * A pointer to a maven dependency
 *
 * @author brad
 */
public class UnderlayVector implements Serializable {

    private static final long serialVersionUID = 1L;
    private String groupId;
    private String artifcatId;
    private String version;

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the artifcatId
     */
    public String getArtifcatId() {
        return artifcatId;
    }

    /**
     * @param artifcatId the artifcatId to set
     */
    public void setArtifcatId(String artifcatId) {
        this.artifcatId = artifcatId;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return groupId + ", " + artifcatId + ", " + version;
    }
}
