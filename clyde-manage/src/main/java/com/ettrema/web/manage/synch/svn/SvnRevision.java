package com.ettrema.web.manage.synch.svn;

import java.util.Map;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;

public class SvnRevision {
    final SvnRepo repo;
    final MessageListener listener;
    
    long rev;
    
    SvnRevision(SvnRepo repo, long revision) {
        this.repo = repo;
        rev = revision;
        listener = repo.listener;
    }
    
    public SvnFolder getFolder(String path) {
        try {
            long fileRevision = repo.repository.getDir(path, rev, (SVNProperties) null, (ISVNDirEntryHandler) null);
            return new SvnFolder(this,path, fileRevision);
        } catch (SVNException ex) {
            System.out.println("not found: " + path + " in rev: " + rev);
            return null;
        }             
    }
    
    public SvnResource getResource(String path) {
        String[] arr = path.split("/");
        String folderPath = "";
        for( int i=0; i<arr.length-1; i++ ) {
            if( arr[i] != null && arr[i].length()>0 ) {
                folderPath += "/" + arr[i];
            }
        }
        SvnFolder folder = getFolder(folderPath);
        SvnResource res = folder.child(arr[arr.length-1]);
        return res;
    }    

	@Override
    public String toString() {
        return "rev-" + rev;
    }
    

}