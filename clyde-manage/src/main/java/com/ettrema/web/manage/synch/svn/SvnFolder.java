package com.bradmcevoy.web.manage.synch.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;


public class SvnFolder extends SvnResource{
    private List<SvnResource> children;    
        
    SvnFolder(SvnRevision revision, String path, long actualRevison) {
        super(revision, path,actualRevison);
    }
    
    public List<SvnResource> children() {
        if (children == null) {
            children = new ArrayList<SvnResource>();
            Collection entries = new ArrayList();
            try {
                long r = this.actualRevision;
                // long r = revision.rev;
                this.revision.repo.repository.getDir(path, r, null, entries);
            } catch (SVNException ex) {
                return children;
            }
            for(Object o : entries) {
                SVNDirEntry dirEntry = (SVNDirEntry) o;
                SvnResource res = newResource(this,dirEntry);
                children.add(res);
            }
        }
        return children;
    }
    
    SvnResource newResource(SvnFolder parent, SVNDirEntry dirEntry) {
        if (dirEntry.getKind()  == SVNNodeKind.DIR) {
            return new SvnFolder(revision,parent.path + "/" + dirEntry.getRelativePath(), dirEntry.getRevision());
        }  else {
            return new SvnFile(revision,parent.path + "/" + dirEntry.getRelativePath(), dirEntry.getRevision());
        }
    }    

    public SvnResource child(String name) {
        List<SvnResource> list = children();
        if( list == null ) return null;
        for( SvnResource res : list ) {
            if( res.getName().equals(name) ) return res;
        }
        return null;
    }
    
    public SVNURL getUrl() {
        String s = revision.repo.getRepoUrl();
        if( path.startsWith("/") ) {
            s = s + path.substring(1);
        } else {
            s = s + path;
        }
        try {
            return SVNURL.parseURIDecoded(s);
        } catch (SVNException ex) {
            throw new RuntimeException(s,ex);
        }
    }
    
    public void checkout(File dir) {
        SVNURL url = getUrl();
        try {
            SVNUpdateClient updateClient = revision.repo.clientManager.getUpdateClient();
            if( listener != null ) {
                updateClient.setEventHandler(new SvnListener());
            }
            updateClient.setIgnoreExternals(false);
            System.out.println("url: " + url);
            updateClient.doCheckout(url,dir,null,SVNRevision.HEAD,true);
        } catch (SVNException ex) {
            throw new RuntimeException( ex.getMessage() + " : " + url.toString() + " > " + dir.getAbsolutePath(),ex);
        }
    }

    
    public void update(File updateDir) {
        SVNURL url = getUrl();
        try {
            SVNUpdateClient updateClient = revision.repo.clientManager.getUpdateClient();
            if( listener != null ) {
                updateClient.setEventHandler(new SvnListener());
            }
            updateClient.doUpdate(updateDir,SVNRevision.HEAD,true);
        } catch (SVNException ex) {
            throw new RuntimeException( ex.getMessage() + " : " + url.toString() + " > " + updateDir.getAbsolutePath(),ex);
        }
    }
    
    
    class SvnListener implements ISVNEventHandler {
        public void handleEvent(SVNEvent e, double d) throws SVNException {
            listener.onMessage(this.getClass(),e.toString() );
        }

        public void checkCancelled() throws SVNCancelException {
        }        
    }    
}