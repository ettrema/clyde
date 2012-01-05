package com.ettrema.web.console2;

import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeFolder;
import com.ettrema.web.code.CodeResourceFactory;
import com.ettrema.console.Result;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.Resource;
import com.ettrema.httpclient.Utils.CancelledException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class Import extends AbstractConsoleCommand {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Import.class);
	private final CodeResourceFactory codeResourceFactory;

	public Import(List<String> args, String host, String currentDir, ResourceFactory resourceFactory, CodeResourceFactory codeResourceFactory) {
		super(args, host, currentDir, resourceFactory);
		this.codeResourceFactory = codeResourceFactory;
	}

	@Override
	public Result execute() {

		if (args.isEmpty()) {
			return result("not enough arguments");
		}
		String importSource = args.get(0);
		if( !importSource.endsWith("/")) {
			importSource += "/";
		}
		String remoteUser = "";
		if (args.size() > 0) {
			remoteUser = args.get(1);
		}
		String remotePassword = "";
		if (args.size() > 1) {
			remotePassword = args.get(2);
		}
		try {
			return doImport(importSource, remoteUser, remotePassword);
		} catch (URISyntaxException ex) {
			log.error(importSource, ex);
			return result("err: " + importSource + " - " + ex.getMessage());
		}

	}

	private CodeFolder createSubFolder(CodeFolder localFolder, String name) {
		try {
			return (CodeFolder) localFolder.createCollection(name);
		} catch (NotAuthorizedException ex) {
			throw new RuntimeException(ex);
		} catch (ConflictException ex) {
			throw new RuntimeException(ex);
		} catch (BadRequestException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Result doImport(String importRoot, String remoteUser, String remotePassword) throws URISyntaxException {
		URI uri = new URI(importRoot);
		com.ettrema.httpclient.Host remoteHost = new Host(uri.getHost(), "/_code" + uri.getPath(), uri.getPort(), remoteUser, remotePassword, null, null);
		StringBuilder sb = new StringBuilder();
		try {
			// copy all items in remoteHost to into the current folder
			Folder currentFolder = currentResource();
			CodeFolder codeFolder = codeResourceFactory.wrapCollection(currentFolder);
			doImport(remoteHost, codeFolder, sb);
		} catch (IOException ex) {
			return result("Interrupted transfer, some files may have been imported: </br>" + sb);
		} catch (HttpException ex) {
			return result("HTTP error, some files may have been imported: </br>" + sb);
		}
		return result("Imported ok<br/>" + sb);
	}

	private void doImport(com.ettrema.httpclient.Folder remoteFolder, CodeFolder localFolder, StringBuilder sb) throws IOException, HttpException {
		log.trace("doImport: " + remoteFolder.href());
		Set<String> skips = new HashSet<String>();

		//Check for a folder called templates, and import it first if it exists
		Resource rTemplates = remoteFolder.child("templates");
		if (rTemplates != null && rTemplates instanceof com.ettrema.httpclient.Folder) {
			com.ettrema.httpclient.Folder fTemplates = (com.ettrema.httpclient.Folder) rTemplates;
			CodeFolder localTemplates = (CodeFolder) localFolder.child("templates");
			if (localTemplates == null) {
				try {
					localTemplates = (CodeFolder) localFolder.createCollection("templates");
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			doImport(fTemplates, localTemplates, sb);
			skips.add(fTemplates.name);
		}

		// Now check for users, this should be in place before processing other folders so
		// that security rules can be linked
		Resource rUsers = remoteFolder.child("users");
		if (rUsers != null && rUsers instanceof com.ettrema.httpclient.Folder) {
			com.ettrema.httpclient.Folder fUsers = (com.ettrema.httpclient.Folder) rUsers;
			CodeFolder localUsers = (CodeFolder) localFolder.child("users");
			if (localUsers == null) {
				try {
					localUsers = (CodeFolder) localFolder.createCollection("users");
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			doImport(fUsers, localUsers, sb);
			skips.add(fUsers.name);
		}

		//import meta files in this folder
		int retryCount = 0;
		boolean allDone = false;
		while (!allDone) {
			try {
				importFiles(remoteFolder, localFolder, true, sb);
				allDone = true;
			} catch (Exception ex) {
				retryCount++;
				if (retryCount > 3) {
					sb.append("<br/><span style='color: red'>Error importing folder, exceeded retries so giving up</span>");
					break;
				} else {
					sb.append("<br/><span style='color: red'>Error importing folder, will retry..</span>");
				}
				log.error("Errors importing folder. Retry count: " + retryCount, ex);
			}
		}
		try {
			//Now import content files in this folder
			importFiles(remoteFolder, localFolder, false, sb);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		//Finally process sub folders. Note that local folders will have been created already due to their meta files
		for (Resource r : remoteFolder.children()) {
			if (!skips.contains(r.name)) {
				if (r instanceof com.ettrema.httpclient.Folder) {
					com.ettrema.httpclient.Folder remoteChildFolder = (com.ettrema.httpclient.Folder) r;
					CodeFolder newLocalFolder = (CodeFolder) localFolder.child(remoteChildFolder.name);
					if (newLocalFolder == null) {
						sb.append("<br/><span style='color: red'>Local folder was not created: ").append(remoteChildFolder.href()).append(" in ").append(localFolder.getName()).append("</span>");
						newLocalFolder = createSubFolder(localFolder, remoteChildFolder.name);
					}
					doImport(remoteChildFolder, newLocalFolder, sb);
				}
			} else {
				log.trace("skipping already processed folder: " + r.name);
			}
		}
	}

	private void importFiles(com.ettrema.httpclient.Folder remoteFolder, CodeFolder localFolder, boolean metaNotMeta, StringBuilder sb) throws Exception {
		boolean errorsOccured = false;
		for (Resource r : remoteFolder.children()) {
			if (r instanceof com.ettrema.httpclient.Folder) {
			} else {
				boolean isMeta = codeResourceFactory.isMeta(r.name);
				boolean doIt = (metaNotMeta && isMeta) || (!metaNotMeta && !isMeta);
				if (doIt) {
					com.ettrema.httpclient.File remoteFile = (com.ettrema.httpclient.File) r;
					try {
						importFile(remoteFile, localFolder, sb);
					} catch (HttpException ex) {
						errorsOccured = true;
						sb.append("<br/><span style='color: red'>Error importing: ").append(r.href()).append(" - ").append(ex.getMessage()).append("</span>");
						log.error("Error importing: " + ex.getMessage() + " Local: " + localFolder.getName(), ex);
					}
				}
			}
		}
		if (errorsOccured) {
			throw new Exception("Errors occured importing");
		}
	}

	private void importFile(com.ettrema.httpclient.File remoteFile, CodeFolder localFolder, StringBuilder sb) throws HttpException {
		// create or replace a file called remoteFile.name in localFolder		
		log.trace("importFile: " + remoteFile.href());
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			remoteFile.download(bout, null);
		} catch (CancelledException ex) {
			throw new RuntimeException(ex);
		}
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		try {
			com.bradmcevoy.http.Resource r = localFolder.child(remoteFile.name);
			if (r != null) {
				if (r instanceof ReplaceableResource) {
					ReplaceableResource replaceable = (ReplaceableResource) r;
					replaceable.replaceContent(bin, (long) bout.size());
					sb.append("<br/>Updated: ").append(localFolder.getName()).append("/").append(r.getName()).append(" (").append(r.getClass()).append(")");
				} else {
					throw new RuntimeException("Object is not replaceable: " + r.getClass());
				}
			} else {
				r = localFolder.createNew(remoteFile.name, bin, (long) bout.size(), remoteFile.contentType);
				sb.append("<br/>Created: ").append(localFolder.getName()).append("/").append(r.getName()).append(" (").append(r.getClass()).append(")");
			}
		} catch (Exception e) {
			sb.append("<br/><span style='color: red'>Error importing: ").append(remoteFile.href()).append(" - ").append(e.getMessage()).append("</span>");
			log.error("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			log.error("Exception importing code", e);
			log.error(bout.toString());
			log.error("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
		}

	}
}
