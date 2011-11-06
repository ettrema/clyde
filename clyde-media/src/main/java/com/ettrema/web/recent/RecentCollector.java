package com.ettrema.web.recent;

import com.ettrema.web.recent.RecentDao.RecentActionType;
import com.ettrema.web.recent.RecentDao.RecentResourceType;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author bradm
 */
public interface RecentCollector {

	public void process(UUID nameId, UUID updatedById, Date dateModified, String targetHref, String targetName, String updatedByName, RecentResourceType resourceType, RecentActionType actionType, String moveDestHref);
	
}
