package com.ettrema.web.recent;

import com.ettrema.event.PostSaveEvent;
import com.ettrema.event.DeleteEvent;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.bradmcevoy.http.HttpManager;
import com.ettrema.db.Table;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.event.MoveEvent;
import com.ettrema.media.DaoUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.BinaryFile;
import com.ettrema.web.Folder;
import com.ettrema.web.IUser;
import com.ettrema.web.Web;
import com.ettrema.web.recent.RecentDao.RecentActionType;
import com.ettrema.web.recent.RecentDao.RecentResourceType;
import com.ettrema.web.security.CurrentUserService;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class RecentManager implements EventListener, TableDefinitionSource {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentManager.class);
	private final CurrentUserService currentUserService;
	private final RecentDao recentDao;

	public RecentManager(EventManager eventManager, CurrentUserService currentUserService) {
		this.currentUserService = currentUserService;
		this.recentDao = new RecentDao();
		eventManager.registerEventListener(this, PostSaveEvent.class);
		eventManager.registerEventListener(this, DeleteEvent.class);
		eventManager.registerEventListener(this, MoveEvent.class);
	}

	public void search(UUID ownerId, Date since, RecentCollector collector) {
		recentDao.search(ownerId.toString(), since, collector);
	}

	public void search(UUID ownerId, String path, RecentCollector collector) {
		recentDao.search(ownerId.toString(), path, collector);
	}

	@Override
	public void onEvent(Event e) {
		log.trace("onEvent");

		if (e instanceof PostSaveEvent) {
			log.trace("onEvent: PostSaveEvent");
			PostSaveEvent pse = (PostSaveEvent) e;
			if (!(pse.getResource() instanceof BaseResource)) {
				log.trace("not a baseresource");
				return;
			}
			BaseResource res = (BaseResource) pse.getResource();
			recordAction(res, RecentDao.RecentActionType.update, "");
		} else if (e instanceof DeleteEvent) {
			log.trace("onEvent: DeleteEvent");
			DeleteEvent de = (DeleteEvent) e;
			if (!(de.getResource() instanceof BaseResource)) {
				return;
			}
			BaseResource res = (BaseResource) de.getResource();

			recordAction(res, RecentDao.RecentActionType.delete, "");
		} else if (e instanceof MoveEvent) {
			log.trace("onEvent: MoveEvent");
			MoveEvent de = (MoveEvent) e;
			if (!(de.getResource() instanceof BaseResource)) {
				return;
			}
			BaseResource res = (BaseResource) de.getResource();
			Folder fDest = (Folder) de.getDestCollection();
			String moveDestHref = fDest.getHref() + de.getNewName();
			recordAction(res, RecentDao.RecentActionType.move, moveDestHref);
		}
	}

	private boolean recordAction(BaseResource res, RecentActionType actionType, String moveDestHref) {
		if (HttpManager.request() == null) {
			log.trace("no current request, so dont create a recent file");
			return true;
		}
		IUser user = currentUserService.getSecurityContextUser();
		Folder parent = res.getParent();
		if (parent != null && parent.isSystemFolder()) {
			log.trace("not creating recent, because parent is a system folder");
			return true;
		}
		if (res instanceof Web) {
			log.trace("not creating recent for Web");
			return true;
		}
		log.trace("create recent record");
		UUID ownerId = DaoUtils.getOwnerId(res);
		UUID userId = null;
		String userName = null;
		if (user != null) {
			userId = user.getNameNodeId();
			userName = user.getName();
		}
		Date dateMod = new Date();
		String targetHref = res.getHref();
		String targetName = res.getName();
		RecentDao.RecentResourceType resourceType = getResourceType(res);
		recentDao.insert(res.getNameNodeId(), ownerId, userId, dateMod, targetHref, targetName, userName, resourceType, actionType, moveDestHref);
		return false;
	}

	private RecentResourceType getResourceType(BaseResource res) {
		if (res instanceof Folder) {
			return RecentResourceType.folder;
		} else if (res instanceof BinaryFile) {
			return RecentResourceType.binary;
		} else {
			return RecentResourceType.other;
		}
	}
	
	@Override
	public List<? extends Table> getTableDefinitions() {
		List<Table> list = new ArrayList<Table>();
		list.add(RecentDao.recentTable);
		return list;
	}

	@Override
	public void onCreate(Table t, Connection con) {
		
	}		
}
