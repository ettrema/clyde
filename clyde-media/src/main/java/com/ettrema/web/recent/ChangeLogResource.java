package com.ettrema.web.recent;

import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.media.DaoUtils;
import com.ettrema.utils.ClydeUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.User;
import com.ettrema.web.recent.RecentDao.RecentActionType;
import com.ettrema.web.recent.RecentDao.RecentResourceType;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Retrieves all change logs for the owner resource. This is to support file
 * syncronisation.
 * 
 * The data is sent in an RSS similar form. It uses non-standard "moved" and "deleted"
 * elements to represent file system changes.
 *
 * @author bradm
 */
public class ChangeLogResource extends AbstractRssResource {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ChangeLogResource.class);
	
	private final RecentManager recentManager;

	public ChangeLogResource(Folder folder, String name, RecentManager recentManager) {
		super(folder, name);
		this.recentManager = recentManager;
	}


	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
		log.trace("sendContent1");
		String sSince = params.get("since");
		Date since = null;
		if (sSince != null) {
			try {
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
				since = df.parse(sSince);
			} catch (ParseException ex) {
				log.warn("Couldnt parse date: " + sSince);
			}
		}
		if( since == null ) {
			since = new Date(0); // since may not be null in DAO
		}

		XmlWriter writer = new XmlWriter(out);
		writer.writeXMLHeader();
		final Element elChannel = writer.begin("rss").writeAtt("version", "2.0").begin("channel").prop("title", folder.getTitle()).prop("link", folder.getHref());
		UUID ownerId = DaoUtils.getOwnerId(folder);
		
		recentManager.search(ownerId, since, new RecentCollector() {

			@Override
			public void process(UUID nameId, UUID updatedById, Date dateModified, String targetHref, String targetName, String updatedByName, RecentResourceType resourceType, RecentActionType actionType, String moveDestHref) {
				if (actionType.equals(RecentActionType.update)) {
					BaseResource r = ClydeUtils.loadResource(nameId);
					if (r != null) {
						appendFile(r, elChannel);
					}
				} else if( actionType.equals(RecentActionType.delete)) {
					// don't look up resource because it might have been physically deleted
					appendDeleted(targetName, targetHref, dateModified, elChannel);
				} else if( actionType.equals(RecentActionType.move)) {
					appendMoved(targetName, targetHref, dateModified, moveDestHref, elChannel);
				}
			}
		});

		elChannel.close().close();

		writer.flush();
	}	
	
	private void appendDeleted(String name, String href, Date modDate, Element elChannel) {
		Element elImg = elChannel.begin("deleted").prop("title", name).prop("link", href).prop("pubDate", formatDate(modDate)).prop("url", href);
		elImg.close(true);
	}

	private void appendMoved(String name, String href, Date modDate, String movedToUrl, Element elChannel) {
		Element elImg = elChannel.begin("moved").prop("title", name).prop("link", href).prop("pubDate", formatDate(modDate)).prop("movedTo", movedToUrl);
		elImg.close(true);
	}
	
	private void appendFile(BaseResource page, Element elChannel) {
		User creator = page.getCreator();
		String author = null;
		if (creator != null) {
			author = creator.getName();
		}
		Element elItem = elChannel.begin("item").prop("title", page.getName()).prop("link", page.getHref()).prop("pubDate", formatDate(page.getModifiedDate()));

		if (author != null) {
			elItem.prop("author", author);
		}
		elItem.close();
	}
	
}
