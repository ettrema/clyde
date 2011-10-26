package com.ettrema.web.manage.synch.svn;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Iterator;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNLocationEntry;

public class SvnFile extends SvnResource {

	SvnFile(SvnRevision revision, String path, long actualRevision) {
		super(revision, path, actualRevision);
	}

	public byte[] getContent() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			//this.revision.repo.repository.getFile(path,actualRevision,null,baos);            
			long[] revs = new long[]{actualRevision};
			Collection col = this.revision.repo.repository.getLocations(path, (Collection) null, revision.rev, revs);
			if (col == null || col.isEmpty()) {
				throw new NullPointerException("didnt find " + path + " " + actualRevision);
			}
			Iterator it = col.iterator();
			SVNLocationEntry entry = null;
			while (it.hasNext()) {
				entry = (SVNLocationEntry) it.next();
			}

//            System.out.println("retrieving from path: " + entry.getPath() + " rev " + entry.getRevision());
			this.revision.repo.repository.getFile(entry.getPath(), entry.getRevision(), null, baos);
		} catch (SVNException ex) {
			throw new RuntimeException(path + " revision " + actualRevision, ex);
		}
		return baos.toByteArray();
	}
}