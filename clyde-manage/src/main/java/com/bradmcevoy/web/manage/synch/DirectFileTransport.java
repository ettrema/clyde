package com.bradmcevoy.web.manage.synch;

import java.util.Date;
import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.code.CodeResourceFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.IOUtils;


/**
 *
 * @author brad
 */
public class DirectFileTransport  implements FileTransport{

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileLoader.class);
	
	private final CodeResourceFactory resourceFactory;
	
	private String hostName = "test.ppod.com";  // todo: fix config to use localhost

	public DirectFileTransport(String hostName, CodeResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
		this.hostName = hostName;
	}
	
	public DirectFileTransport(CodeResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
		this.hostName = "test.com";
	}
	
	
    /**
     * Just upload the given file to its parent directory.
     * No name transformations
     * @param f
     */
	@Override
    public void put(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.trace("put: " + f.getAbsolutePath());
        CollectionResource colParent = findCollection(f.getParentFile(), root);
        if( colParent == null ) {
            throw new RuntimeException("Couldnt locate parent: " + f.getParentFile().getAbsolutePath() + " for root: " + root.getAbsolutePath());
        }
        Resource rExisting = colParent.child(f.getName());
        FileInputStream fin = null;
        if (rExisting instanceof ReplaceableResource) {
            log.trace("replace content");
            ReplaceableResource replaceable = (ReplaceableResource) rExisting;
            try {
                fin = new FileInputStream(f);
                replaceable.replaceContent(fin, f.length());
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } finally {
                IOUtils.closeQuietly(fin);
            }
        } else {
            if (rExisting != null) {
                log.trace("resource found, but is not replaceable. create new resource: " + rExisting.getClass());
            } else {
                log.trace("no resource found, create new resource");
            }
            if (colParent instanceof PutableResource) {
                PutableResource putable = (PutableResource) colParent;
                try {
                    fin = new FileInputStream(f);
                    String ct = ContentTypeUtils.findContentTypes(f);
                    putable.createNew(f.getName(), fin, f.length(), ct);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    IOUtils.closeQuietly(fin);
                }
            } else {
                throw new RuntimeException("Can't upload, parent folder doesnt support PUT: " + colParent.getName() + " - " + colParent.getClass());
            }
        }
    }

    private CollectionResource findCollection(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException {
        String url = CodeSynchUtils.toCodePath(f, root);
        if (url == null) {
            log.warn("Couldnt calculate url for: " + f.getAbsolutePath() + " from root: " + root.getAbsolutePath());
            return null;
        }
        Resource r = resourceFactory.getResource(hostName, url);
        CollectionResource col;
        if (r == null) {
            log.trace("not found: " + url);
            Resource rParent = findCollection(f.getParentFile(), root);
            if (rParent == null) {
                throw new RuntimeException("Couldnt get parent: " + f.getAbsolutePath());
            } else if (rParent instanceof MakeCollectionableResource) {
                MakeCollectionableResource mkcol = (MakeCollectionableResource) rParent;
                col = mkcol.createCollection(f.getName());
                return col;
            } else {
                throw new RuntimeException("Cant create " + f.getAbsolutePath() + " parent doesnt support MKCOL");
            }
        } else {
            if (r instanceof CollectionResource) {
                return (CollectionResource) r;
            } else {
                throw new RuntimeException("Found resource but its not a collection: " + f.getAbsolutePath() + "  maps to a: " + r.getClass());
            }
        }


    }

	@Override
    public  void delete(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace("delete: " + f.getAbsolutePath());
        File fMeta = CodeSynchUtils.toMetaFile(f);
        Resource r = resourceFactory.getResource(hostName, CodeSynchUtils.toCodePath(fMeta, root));
        if (r == null) {
            log.trace("not found to delete");
        } else if (r instanceof DeletableResource) {
            DeletableResource dr = (DeletableResource) r;
            dr.delete();
        } else {
            throw new RuntimeException("Cannot delete " + f.getAbsolutePath() + " is a : " + r.getClass());
        }

    }	
	
	@Override
    public boolean isNewOrUpdated(File f, File root) {
        f = CodeSynchUtils.toMetaFile(f);
        Resource r = resourceFactory.getResource(hostName, CodeSynchUtils.toCodePath(f, root));
        if (r == null || r.getModifiedDate() == null) {
            return true;
        } else {
            Date fileModDate = new Date(f.lastModified());
            return fileModDate.after(r.getModifiedDate());
        }
    }
	
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }	
	
}