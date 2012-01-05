package com.ettrema.patches;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.Organisation;
import com.ettrema.web.User;
import com.ettrema.common.Service;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.List;

/**
 *
 * @author brad
 */
public class RootUserCreator implements Service {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RootUserCreator.class);
	private final RootContext rootContext;
	private String hostName;
	private String userName;
	private String password;
	private String templateName;

	public RootUserCreator(RootContext rootContext, String hostName, String userName, String password, String templateName) {
		this.rootContext = rootContext;
		this.hostName = hostName;
		this.userName = userName;
		this.password = password;
		this.templateName = templateName;
	}

	private void checkAndCreate(Context context) {
		VfsSession sess = context.get(VfsSession.class);
		List<NameNode> list = sess.find(Organisation.class, hostName);
		if (list == null || list.isEmpty()) {
			log.debug("no organisation found: " + hostName);
			return;
		}
		for (NameNode nn : list) {
			Organisation org = (Organisation) nn.getData();
			if (org == null) {
				log.warn("node contains null data object: " + nn.getId());
			} else {
				Resource r = org.getUsers(true).child(userName);
				if (r == null) {
					log.debug("creating user: " + userName + " in organisation: " + org.getPath());
					User u = org.createUser(userName, password);
					u.setTemplateName(templateName);
					u.save();
				} else {
					log.debug("found an existing resource: " + r.getClass());
				}
			}
		}
		sess.commit();

	}

	private void checkAndCreate() {
		rootContext.execute(new Executable2() {

			@Override
			public void execute(Context context) {
				try {
					checkAndCreate(context);
				} catch (Exception e) {
					log.error("Exception checking for root user, will continue anyway...", e);
				}
			}
		});

	}

	@Override
	public void start() {
		checkAndCreate();
	}

	@Override
	public void stop() {
	}
}
