package com.bradmcevoy.binary;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.io.BufferingOutputStream;
import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.utils.ClydeUtils;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.OutputStreamWriter;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.Web;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * Always set the first version directly on the resource (using the wrapped
 * service) for the sake of efficiency (most resources will only ever have
 * one version)
 *
 * After first version write each version to a version node within a versions node
 *
 * When reading, check for the existence of the versions node to determine whether
 * to delegate or not
 *
 */
public class VersioningBinaryManagerService implements ClydeBinaryService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VersioningBinaryManagerService.class);

    private final ClydeBinaryService wrapped;

    /**
     * Delegates to the given service for reads when no versions are found
     *
     * @param wrapped
     */
    public VersioningBinaryManagerService(ClydeBinaryService wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Does not used a wrapped version manager
     */
    public VersioningBinaryManagerService() {
        wrapped = null;
    }



    public int setContent(BinaryFile file, InputStream in) {
        log.debug("setContent: " + file.getName());
        if( !file.isFirstVersionDone() || !isVersioningEnabled(file) ) {
            // Set the first version directly on the file's node, using the wrapped service
            log.debug("delegate");
            file.setFirstVersionDone(true);
            return wrapped.setContent(file, in);
        } else {
            // Use versioning for subsequent versions
            log.debug("do versioning: " + file.getLocalContentLength());
            NameNode versionsNode = getVersionsNode(file, true); // create the node
            Version v = createVersionNode(versionsNode);

            CheckedInputStream cin = new CheckedInputStream(in, new CRC32());
            int contentLength = (int) v.nameNode().setBinaryContent(cin);
            if (contentLength == 0) {
                log.warn("zero size file: " + file.getHref());
            }
            v.setContentLength(contentLength);
            long crc = cin.getChecksum().getValue();
            v.setCrc(crc);

            IUser currentUser;
            Auth auth = HttpManager.request().getAuthorization();
            if (auth != null && auth.getTag() != null && auth.getTag() instanceof IUser) {
                currentUser = (IUser) auth.getTag();
                v.setUserId(currentUser.getNameNodeId());
            }

            v.save();
            return contentLength;
        }
    }

    public int writeToOutputStream(BinaryFile file, OutputStreamWriter<Long> writer) {
        BufferingOutputStream bout = new BufferingOutputStream(50000);
        try {
            writer.writeTo(bout);
        } finally {
            FileUtils.close(bout);
        }
        InputStream in = null;
        try {
            in = bout.getInputStream();
            return setContent(file, in);
        } finally {
            FileUtils.close(in);
        }
    }

    public InputStream readInputStream(BinaryFile file, String versionNum) throws BadRequestException {
        NameNode versionsNode = getVersionsNode(file, false);
        Version v;
        if( versionNum == null || versionNum.length() == 0) {
            v = getLatestVersion(versionsNode);
        } else {
            v = getNamedVersion(versionsNode, versionNum);
            if( v == null ) {
                throw new BadRequestException(file, "Version not found: " + versionNum);
            }
        }
        if( v == null ) {
            if(wrapped == null) {
                return null;
            } else {
                return wrapped.readInputStream(file, versionNum);
            }
        } else {
            return v.nameNode().getBinaryContent();
        }

    }

    public long getContentLength(BinaryFile file, String versionNum) {
        NameNode versionsNode = getVersionsNode(file, false);
        Version v = getNamedVersion(versionsNode, versionNum);
        if( v == null ) {
            if(wrapped == null) {
                return 0;
            } else {
                return wrapped.getContentLength(file, versionNum);
            }
        } else {
            return v.getContentLength();
        }
    }

    public long getCrc(BinaryFile file, String versionNum) {
        NameNode versionsNode = getVersionsNode(file, false);
        Version v = getNamedVersion(versionsNode, versionNum);
        if( v == null ) {
            if(wrapped == null) {
                return 0;
            } else {
                return wrapped.getCrc(file, versionNum);
            }
        } else {
            return v.getCrc();
        }

    }

    private NameNode getVersionsNode(BinaryFile file, boolean create) {
        NameNode versionsNode = file.getNameNode().child("_versions");
        if (versionsNode == null) {
            if (create) {
                log.debug("create versions node");
                versionsNode = file.getNameNode().add("_versions", new Versions());
                versionsNode.save();
                return versionsNode;
            } else {
                return null;
            }
        } else {
            return versionsNode;
        }

    }

    /**
     * Create and save a new version data node
     *
     * @param versionsNode
     * @return
     */
    private Version createVersionNode(NameNode versionsNode) {
        String nm = ClydeUtils.pad(versionsNode.children().size() + 1);
        Version v = new Version();
        NameNode n = versionsNode.add(nm, v);
        n.save();
        log.debug("created version node: " + n.getName());
        return v;
    }

    /**
     * Find the version node with the latest create date
     *
     * @param versionsNode
     * @return
     */
    private Version getLatestVersion(NameNode versionsNode) {
        if( versionsNode == null ) {
            return null;
        }
        List<NameNode> list = versionsNode.children();
        Date latest = null;
        NameNode latestNode = null;
        for( NameNode n : list ) {
            if( n.getDataClass().equals(Version.class)) {
                if( latest == null || n.getCreatedDate().after(latest)) {
                    latest = n.getCreatedDate();
                    latestNode = n;
                }
            }
        }
        return (Version) latestNode.getData();
    }

    public List<VersionDescriptor> getVersions(BinaryFile file) {
        List versions = wrapped.getVersions(file);
        NameNode versionsNode = getVersionsNode(file, false);
        if( versionsNode != null ) {
            log.debug("children: " + versionsNode.children().size());
            for(NameNode n : versionsNode.children() ) {
                log.debug("version node: " + n.getName());
                DataNode dn = n.getData();
                if( dn != null ) {
                    if( dn instanceof Version) {
                        versions.add((Version)dn);
                    } else {
                        log.warn("not a version node: " + dn.getClass().getCanonicalName());
                    }
                } else {
                    log.warn("node in versions has no data node: " + n.getId());
                }
            }
        }
        log.debug("getVersions: " + versions.size());
        return versions;
    }

    private Version getNamedVersion(NameNode versionsNode, String versionNum) {
        if( versionsNode == null ) {
            return null;
        }
        if( versionNum == null || versionNum.length() == 0 ) {
            return getLatestVersion(versionsNode);
        } else {
            NameNode n =  versionsNode.child(versionNum);
            if( n == null) {
                return null;
            } else if( n.getDataClass().equals(Version.class)) {
                return (Version) n.getData();
            } else {
                return null;
            }
        }
    }

    private boolean isVersioningEnabled(BinaryFile file) {
        return isVersioningEnabled(file.getParentFolder());
    }

    private boolean isVersioningEnabled(Folder folder) {
        Boolean b = folder.isVersioningEnabled();
        if( b == null ) {
            if( folder instanceof Web) {
                return true; // No value specified, so default to true
            } else {
                return isVersioningEnabled(folder.getParentFolder());
            }
        } else {
            return b.booleanValue();
        }
    }
}
