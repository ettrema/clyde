package com.bradmcevoy.web.manage.synch;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.ProgressListener;
import com.ettrema.httpclient.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author brad
 */
public class HttpFileTransport implements FileTransport {
	
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HttpFileTransport.class);
	
	private Host host;

	public HttpFileTransport(String hostName, int port, String user, String pwd) {
		host = new Host(hostName, "/_code" , port, user, pwd, null, null);
	}
	

	@Override
	public void put(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException {
        log.trace("put: " + f.getAbsolutePath());
		String url = CodeSynchUtils.toPath(f, root);
		host.doPut(Path.path(url), f, new ConsoleProgressListener());
	}

	@Override
	public void delete(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException {
		String path = CodeSynchUtils.toPath(f, root);
		String url = host.getHref(Path.path(path));
		try {			
			host.doDelete(url);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (HttpException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean isNewOrUpdated(File f, File root) {
		String path = CodeSynchUtils.toPath(f, root);
		Resource r;
		try {
			r = host.find(path);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (HttpException ex) {
			throw new RuntimeException(ex);
		}
		if( r == null ) {
			return true;
		} else {
            Date fileModDate = new Date(f.lastModified());
            return fileModDate.after(r.getModifiedDate());
		}
	}
	
	private class ConsoleProgressListener implements ProgressListener {

		@Override
		public void onRead(int bytes) {
			
		}

		@Override
		public void onProgress(long bytesRead, Long totalBytes, String fileName) {
			String progress;
			if( totalBytes !=null && totalBytes > 0) {
				progress = bytesRead/totalBytes + "%";
			} else {
				progress = totalBytes + " bytes";
			}
			System.out.print("  " + progress);
		}

		@Override
		public void onComplete(String fileName) {
			log.info("Finished uploadeding: " + fileName);
		}

		@Override
		public boolean isCancelled() {
			return false;
		}
		
	}
}
