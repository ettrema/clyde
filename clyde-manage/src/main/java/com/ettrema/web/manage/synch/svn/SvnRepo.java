package com.bradmcevoy.web.manage.synch.svn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 *
 * @author mcevoyb
 */
public class SvnRepo {
    
    {
        DAVRepositoryFactory.setup();
    }
    
    SVNClientManager clientManager = SVNClientManager.newInstance();
    SVNRepository repository = null;
    
//    static final String SVN_BASE_URL = "https://projects.dev.internal/svn/";
    
    final MessageListener listener;
    String repoUrl;
    
    public SvnRepo(MessageListener listener, String repo, String userName, String password) {
        this.listener = ((listener == null) ?  new NullMessageListener() : listener);
        this.repoUrl = repo;
        if (!repoUrl.endsWith("/")) {
          repoUrl += "/";
        }
        try {
            this.listener.onMessage(this.getClass(),"Connecting to SVN: " + repo);
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repo));
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
        
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
        repository.setAuthenticationManager(authManager);
    }
    
    public String getRepoUrl() {
        return repoUrl;
    }
    
    public SvnRevision getRevision(long revision) {
        return new SvnRevision(this, revision);
    }
    
    public SvnRevision getLatestRevision() {
        return new SvnRevision( this, getLatestRevisionId() );
    }
    
    public SvnRevision getHeadRevision() {
        //return new Revision(-1);
        return getLatestRevision();
    }
    
    public SvnRevision[] getRevisionsForPath(String path) {
        Collection col;
        try {
            long endRevision = getLatestRevisionId();
            col = repository.getFileRevisions(path, null, -1, endRevision);
        } catch (SVNException ex) {
            System.out.println("path not found: " + path + " ex: " + ex.getMessage());
            listener.onMessage(this.getClass(),"path not found: " + path + " ex: " + ex.getMessage());
            return null;
            //throw new RuntimeException(path,ex);
        }
        List<SvnRevision> revisions = new ArrayList<SvnRevision>();
        for( Object o : col ) {
            SVNFileRevision rev = (SVNFileRevision) o;
            revisions.add( new SvnRevision(this,rev.getRevision()));
//            if( path.equals(rev.getPath()) ) {
//                revisions.add( new SvnRevision(this,rev.getRevision()));
//            } else {
//                System.out.println("not including other path: " + rev.getPath());
//            }
        }
        
        
        SvnRevision[] arr = new SvnRevision[revisions.size()];
        revisions.toArray(arr);
        return arr;
    }


    
    public long getLatestRevisionId() {
        long r;
        try {
            r = repository.getLatestRevision();
            return r;
        } catch (SVNException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    public AssociatedRevisions[] getAssociatedRevisions(String path1, String path2) {
        Collection col1;
        try {
            long endRevision = getLatestRevisionId();
            col1 = repository.getFileRevisions(path1, null, -1, endRevision);
        } catch (SVNException ex) {
            System.out.println("WARN: path not found: " + path1 + " ex: " + ex.getMessage());
            listener.onMessage(this.getClass(),"WARN: path not found: " + path1 + " ex: " + ex.getMessage());
            return null;
//            throw new RuntimeException(path1,ex);
        }

        Collection col2;
        try {
            long endRevision = getLatestRevisionId();
            col2 = repository.getFileRevisions(path2, null, -1, endRevision);
        } catch (SVNException ex) {
            System.out.println("WARN: path not found: " + path2 + " ex: " + ex.getMessage());
            listener.onMessage(this.getClass(),"WARN: path not found: " + path2 + " ex: " + ex.getMessage());
//            throw new RuntimeException(path2,ex);
            return null;
        }

        List<AssociatedRevisions> list = new ArrayList<AssociatedRevisions>();
        for( Object o : col1 ) {
            SVNFileRevision rev = (SVNFileRevision) o;
            SvnRevision revi = new SvnRevision(this,rev.getRevision());
            SvnFile file1 = new SvnFile(revi,rev.getPath(),rev.getRevision());
            SVNFileRevision file2Rev = getFile2(col2,rev.getRevision());
            if( file2Rev != null ) {
                SvnFile file2 = new SvnFile(revi,file2Rev.getPath(),file2Rev.getRevision());
                list.add( new AssociatedRevisions(file1,file2) );
            }
        }
        AssociatedRevisions[] arr = new AssociatedRevisions[list.size()];
        return list.toArray(arr);
    }

    
    public class AssociatedRevisions {
        public final SvnFile file1;
        public final SvnFile file2;
        
        AssociatedRevisions(SvnFile file1, SvnFile file2) {
            this.file1 = file1;
            this.file2 = file2;
        }
    }
    
    
    /** Find the version of file2 where its revision is the maximum less then
     *  or equal to the given revision number
     */
    private SVNFileRevision getFile2(Collection col2, long r) {
        SVNFileRevision max = null;
        for( Object o : col2 ) {
            SVNFileRevision rev = (SVNFileRevision) o;
            if( rev.getRevision() <= r ) {
                if( max == null || max.getRevision() < rev.getRevision() ) {
                    max = rev;
                }
            }
        }
        return max;
    }
}
