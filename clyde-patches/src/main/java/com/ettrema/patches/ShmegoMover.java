package com.ettrema.patches;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.security.PasswordStorageService;
import com.ettrema.httpclient.File;
import com.ettrema.vfs.VfsSession;
import com.bradmcevoy.utils.LogUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.XmlPersistableResource;
import com.bradmcevoy.web.console2.PatchApplicator;
import com.ettrema.context.Context;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.ProgressListener;
import com.ettrema.httpclient.Resource;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author bradm
 */
public class ShmegoMover implements PatchApplicator, Serializable {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ShmegoMover.class);
	private String[] args;
	private UUID currentFolderId;
	String server;
	int port;
	String userName;
	String password;
	String accountName;
	
	private int numFiles;

	public void doProcess(Context context) {
		Folder currentResource = (Folder) _(VfsSession.class).get(currentFolderId).getData();
		try {
			LogUtils.info(log, "Move account", server, port, userName);
			Host host = new Host(server, port, userName, password, null);
			migrateUser(host, currentResource.getHost());
			migrateFiles(host, currentResource.getHost());
			System.out.println("******************* Finished - " + numFiles + " files copied  ******************");
			_(VfsSession.class).commit();
		} catch (IOException ex) {
			log.error("Move failed", ex);
		} catch (HttpException ex) {
			log.error("Move failed", ex);
		} catch (Exception ex) {
			log.error("Move failed", ex);
		}
	}

	public String getName() {
		return "Shmego account mover";
	}

	public void setArgs(String[] args) {
		this.args = args;
		server = args[0];
		port = Integer.parseInt(args[1]);
		userName = args[2];
		password = args[3];
		accountName = args[4];
	}

	public void setCurrentFolder(Folder currentResource) {
		this.currentFolderId = currentResource.getNameNodeId();
	}

	public void pleaseImplementSerializable() {
	}

	private void migrateUser(Host remoteHost, com.bradmcevoy.web.Host localHost) throws IOException, HttpException, Exception {
		Resource rUser = remoteHost.find("/users/" + accountName);
		if (rUser == null) {
			throw new Exception("User not found: " + accountName);
		}
		if (!(rUser instanceof com.ettrema.httpclient.Folder)) {
			throw new Exception("Found something, but its not a user: " + rUser.getClass());
		}
		com.ettrema.httpclient.Folder fUser = (com.ettrema.httpclient.Folder) rUser;
		log.info("Moving: " + fUser.href());
		String sourcePath = "/users/" + accountName + ".source";
		String source = new String(remoteHost.get(sourcePath), "UTF-8");
		//System.out.println("source: " + source);
		Folder users = (Folder) localHost.getUsers();
		User newLocalUser;
		if (users.hasChild(accountName)) {
			newLocalUser = (User) users.childRes(accountName);
		} else {
			newLocalUser = localHost.createUser(accountName, "adfgewsaq");
		}
		PasswordStorageService passwordStorageService = _(PasswordStorageService.class);
		boolean enabled = passwordStorageService.isEnabled();
		passwordStorageService.setEnabled(false);
		try {
			replaceContent(source, newLocalUser);
		} finally {
			passwordStorageService.setEnabled(enabled);
		}
	}

	private void migrateFiles(Host remoteHost, com.bradmcevoy.web.Host localHost) throws HttpException, UnsupportedEncodingException, Exception {
		log.info("migrateFiles");
		String customerSiteName = accountName + ".shmego.com";
		com.bradmcevoy.web.Host customerHost = (com.bradmcevoy.web.Host) localHost.find("/sites/" + customerSiteName);
		if (customerHost == null) {
			Folder sites = (Folder) localHost.child("sites");
			customerHost = new com.bradmcevoy.web.Host(sites, customerSiteName);
			customerHost.save();
		}
		String sourcePath = "/sites/" + customerSiteName + ".source";
		String customerHostSource = new String(remoteHost.get(sourcePath), "UTF-8");
		System.out.println(customerHostSource);
		customerHostSource = customerHostSource.replace("withinLimit", "ok");
		customerHostSource = customerHostSource.replace("free", "enabled");
		
		replaceContent(customerHostSource, customerHost);
		com.bradmcevoy.web.Folder localFiles = customerHost.getSubFolder("files");
		if (localFiles == null) {
			localFiles = (Folder) customerHost.createCollection("files", false);
		}
		com.ettrema.httpclient.Folder remoteFiles = (com.ettrema.httpclient.Folder) remoteHost.find("/sites/" + customerSiteName + "/files");
		copyFiles(remoteFiles, localFiles);

	}

	public void replaceContent(String s, XmlPersistableResource res) throws Exception {
		log.info("replaceContent");
		try {
			XmlUtils2 x = new XmlUtils2();
			Document doc = x.getJDomDocument(s);
			Element el = doc.getRootElement();
			el = (Element) el.getChildren().get(0);
			res.loadFromXml(el, null);
			res.save();
		} catch (JDOMException ex) {
			throw new Exception(ex);
		}
	}

	private void copyFiles(com.ettrema.httpclient.Folder remoteFiles, Folder localFiles) throws IOException, HttpException, ConflictException, NotAuthorizedException, BadRequestException {
		for (Resource remoteRes : remoteFiles.children()) {
			if (remoteRes instanceof com.ettrema.httpclient.Folder) {
				if (!ignored(remoteRes)) {
					com.ettrema.httpclient.Folder remoteChildFolder = (com.ettrema.httpclient.Folder) remoteRes;
					Folder localChildFolder = getOrCreateFolder(localFiles, remoteRes.name);
					copyFiles(remoteChildFolder, localChildFolder);
				}
			} else {
				com.ettrema.httpclient.File remoteFile = (com.ettrema.httpclient.File) remoteRes;
				copyFile(remoteFile, localFiles);
			}
		}
	}

	private void copyFile(File remoteFile, Folder localFiles) throws IOException, FileNotFoundException, HttpException, NotAuthorizedException, ConflictException, BadRequestException {
		LogUtils.info(log, "copyFile", remoteFile.href(), localFiles.getHref());
		java.io.File tempDest = java.io.File.createTempFile("shmego-mover", remoteFile.name);
		log.info(" download file: " + formatBytes(tempDest.length()));
		if( tempDest.length() == 0 ) {
			System.out.println("----------------- Zero size file: " + remoteFile.href());
			System.out.println("---------------------------------------------------------");
			return ;
		}
		remoteFile.downloadToFile(tempDest, new ShmegoMoverProgressListener());
		BaseResource localFile = localFiles.childRes(remoteFile.name);
		if (localFile != null) {
			localFile.deleteNoTx();
		}
		FileInputStream fin = null;
		BufferedInputStream bufIn = null;
		try {
			fin = new FileInputStream(tempDest);
			bufIn = new BufferedInputStream(fin);
			localFiles.createNew_notx(remoteFile.name, bufIn, tempDest.length(), remoteFile.contentType);
		} finally {
			IOUtils.closeQuietly(bufIn);
			IOUtils.closeQuietly(fin);
		}
		numFiles++;
	}

	private boolean ignored(Resource remoteRes) {
		if (remoteRes.name.startsWith("_sys")) {
			return true;
		}
		return false;
	}

	private Folder getOrCreateFolder(Folder localFiles, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
		Folder f = localFiles.getSubFolder(name);
		if (f == null) {
			f = (Folder) localFiles.createCollection(name, false);
		}
		return f;
	}

	private class ShmegoMoverProgressListener implements ProgressListener {

		public void onRead(int bytes) {
		}

		public void onProgress(long bytesRead, Long totalBytes, String fileName) {
			System.out.println(formatBytes(bytesRead));
		}

		public void onComplete(String fileName) {
		}

		public boolean isCancelled() {
			return false;
		}
	}

	private String formatBytes(long l) {
		if (l < 1000) {
			return l + " bytes";
		} else if (l < 1000000) {
			return l / 1000 + "KB";
		} else {
			return l / 1000000 + "MB";
		}
	}
}
