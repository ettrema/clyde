package com.ettrema.web.migrate;

import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.http.Auth;
import com.ettrema.context.Context;
import org.jdom.Element;
import java.util.List;
import java.util.UUID;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.migrate.Arguments;
import com.ettrema.migrate.MigrationHelper;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.component.InitUtils;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContextLocator;
import com.ettrema.vfs.NameNode;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

import static com.ettrema.context.RequestContext._;

/**
 * To submit a job, send a form post containing the following fields
 *
 * <input type="checkbox" name="resourceId_1" value="xxxxx"/>
 *
 * @author brad
 */
@BeanPropertyResource(value = "clyde",enableByDefault=false)
public class MigrateResource extends BaseResource {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MigrateResource.class);
	private static final long serialVersionUID = 1L;
	private static final Map<String, Arguments> jobs = new ConcurrentHashMap<String, Arguments>();
	private String remoteHost;
	private int remotePort;
	private String remotePath;
	private String remoteUser;
	private String remotePassword;
	private Path localPath;

	public MigrateResource(String contentType, Folder parentFolder, String newName) {
		super(contentType, parentFolder, newName);
	}

	public MigrateResource(Folder parentFolder, String newName) {
		super(null, parentFolder, newName);
	}

	@Override
	public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
		// TODO: check for admin role
		String command = parameters.get("command");
		if ("submit".equals(command)) {
			submitMigrationJob(parameters);
		} else if ("stop".equals(command)) {
			Arguments args = jobs.get(remoteHost);
			if (args != null) {
				args.cancel();
			}
		}
		return null;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
		String command = params.get("command");
		log.trace("sendContent: " + command);
		PrintWriter pw = new PrintWriter(out);
		if ("query".equals(command)) {
			Arguments args = getFiles();
			try {
				sendJsonData(args, pw);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			// Return the current status, for submitted command and for anything else
			Arguments args = getStatus();
			if (args == null) {
				log.trace("no current job");
				sendJsonData(new ArrayList(), pw);
			} else {
				log.trace("got a current job, so send report");
				sendJsonData(args, pw);
			}

		}
	}

	@com.bradmcevoy.property.BeanPropertyAccess(true)
	public Arguments getStatus() {
		Arguments args = jobs.get(remoteHost);
		return args;
	}

	@com.bradmcevoy.property.BeanPropertyAccess(true)
	public Arguments getFiles() {
		// Query for what files should be migrated
		MigrationHelper migrationHelper = _(MigrationHelper.class);
		Folder localFolder = (Folder) this.getHost().find(localPath);
		if (localFolder == null) {
			throw new RuntimeException("Local folder not found: " + localPath);
		}
		Arguments args = getArgs(localFolder);
		args.setDryRun(true);
		try {
			migrationHelper.doMigration(args);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return args;
	}

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	private void sendJsonData(Object args, PrintWriter pw) {
		JSON json;
		JsonConfig cfg = new JsonConfig();
		cfg.setIgnoreTransientFields(true);
		cfg.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
		json = JSONSerializer.toJSON(args, cfg);
		json.write(pw);
		pw.flush();
	}

	@Override
	protected BaseResource newInstance(Folder parent, String newName) {
		MigrateResource migrateResource = new MigrateResource(parent, newName);
		return migrateResource;
	}

	@Override
	public boolean isIndexable() {
		return false;
	}

	@Override
	public String getDefaultContentType() {
		return null;
	}

	public void onDeleted(NameNode nameNode) {
	}

	private void submitMigrationJob(Map<String, String> parameters) {
		List<UUID> ids = new ArrayList<UUID>();
		for (String param : parameters.keySet()) {
			if (param.startsWith("resourceId_")) {
				UUID uid = UUID.fromString(parameters.get(param));
				ids.add(uid);
			}
		}
		log.info("submitMigrationJob: id list size: " + ids.size());
		final Arguments args = getArgs(ids);
		jobs.put(remoteHost, args);


		final MigrationHelper migrationHelper = _(MigrationHelper.class);
		final RootContextLocator rootContextLocator = _(RootContextLocator.class);
		args.setWorker(new Thread(new Runnable() {

			public void run() {
				rootContextLocator.getRootContext().execute(new Executable2() {

					public void execute(Context context) {
						try {
							migrationHelper.doMigration(args);
						} catch (Exception ex) {
							args.onException(ex);
						} finally {
							args.onFinished();
						}

					}
				});

			}
		}, "Migrate-" + remoteHost));
		args.worker().setDaemon(true);
		args.worker().start();

	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public void setRemotePassword(String remotePassword) {
		this.remotePassword = remotePassword;
	}

	public String remotePassword() {
		return remotePassword;
	}

	public Path getLocalPath() {
		return localPath;
	}

	public void setLocalPath(Path localPath) {
		this.localPath = localPath;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	private Arguments getArgs(Folder localFolder) {
		Arguments args = new Arguments(localFolder, remoteHost, remotePort, remoteUser, remotePassword, remotePath);
		args.setStopAtHosts(true);
		args.setNoUser(true);
		args.setRecursive(true);
		return args;
	}

	private Arguments getArgs(List<UUID> ids) {
		Arguments args = new Arguments(ids, remoteHost, remotePort, remoteUser, remotePassword, remotePath);
		args.setStopAtHosts(true);
		args.setNoUser(true);
		args.setRecursive(true);
		return args;
	}

	@Override
	public void loadFromXml(Element el) {
		super.loadFromXml(el);
		this.localPath = InitUtils.getPath(el, "localFolder");
		this.remoteHost = InitUtils.getValue(el, "remoteHost");
		this.remotePassword = InitUtils.getValue(el, "remotePassword");
		this.remotePath = InitUtils.getValue(el, "remotePath");
		this.remoteUser = InitUtils.getValue(el, "remoteUser");
		this.remotePort = InitUtils.getInt(el, "remotePort");
	}

	@Override
	public void populateXml(Element e2) {
		super.populateXml(e2);
		InitUtils.set(e2, "localFolder", localPath);
		InitUtils.set(e2, "remoteHost", remoteHost);
		InitUtils.set(e2, "remotePort", remotePort);
		InitUtils.set(e2, "remotePassword", remotePassword);
		InitUtils.set(e2, "remotePath", remotePath);
		InitUtils.set(e2, "remoteUser", remoteUser);
	}
}
