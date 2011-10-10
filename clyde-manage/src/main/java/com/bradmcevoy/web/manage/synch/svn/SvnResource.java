package com.bradmcevoy.web.manage.synch.svn;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;

public class SvnResource {
    public final String path;
    public final long actualRevision;
    public final SvnRevision revision;
    
    final MessageListener listener;
    
    SvnResource(SvnRevision revision, String path, long actualRevision) {
        this.revision = revision;
        this.path = path;
        this.actualRevision = actualRevision;
        this.listener = revision.listener;
    }
    
    public String getName() {
        String[] arr = path.split("/");
        return arr[arr.length-1];
    }
    
    public boolean exists() {
        SVNDirEntry entry;
        try {
            entry = revision.repo.repository.info(path, actualRevision);
        } catch (SVNException ex) {
            throw new RuntimeException(path,ex);
        }
        return (entry != null);
    }

	@Override
    public String toString() {
        return path + "(" + actualRevision + ")";
    }   
}