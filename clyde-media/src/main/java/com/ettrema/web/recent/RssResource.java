package com.ettrema.web.recent;

import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.DateUtils.DateParseException;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.BaseResourceList;
import com.ettrema.web.Folder;
import com.ettrema.web.ImageFile;
import com.ettrema.web.ImageFile.ImageData;
import com.ettrema.web.Page;
import com.ettrema.web.User;
import com.ettrema.web.recent.RecentDao.RecentActionType;
import com.ettrema.web.recent.RecentDao.RecentResourceType;
import com.ettrema.media.DaoUtils;
import com.ettrema.utils.ClydeUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Implements an RSS feed. Can be used on a Recent folder because RecentResource's
 * are converted to their targets
 *
 * Has specific support for images and pages
 *
 */
public class RssResource extends AbstractRssResource {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RssResource.class);
	private final RecentManager recentManager;


	public RssResource(Folder folder, String name, RecentManager recentManager) {
		super(folder, name);
		this.recentManager = recentManager;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
		log.trace("sendContent1");

		String where = params.get("where");
		final boolean includeImages;
		final boolean includePages;
		final boolean includeBinaries;
		if (where != null && where.length() > 0) {
			if (where.equals("image")) {
				log.debug("not include pages");
				includePages = false;
				includeBinaries = false;
				includeImages = true;
			} else if (where.equals("page")) {
				log.debug("not include images");
				includeImages = false;
				includeBinaries = false;
				includePages = true;
			} else {
				includeImages = true;
				includeBinaries = true;
				includePages = true;
			}
		} else {
			includeImages = true;
			includeBinaries = true;
			includePages = true;
		}

		BaseResourceList list = getResources();
		list = list.getSortByModifiedDate();
		XmlWriter writer = new XmlWriter(out);
		writer.writeXMLHeader();
		final Element elChannel = writer.begin("rss").writeAtt("version", "2.0").begin("channel").prop("title", folder.getTitle()).prop("link", folder.getHref());
		if (log.isTraceEnabled()) {
			log.trace("sendContent: resources: " + list.size());
		}
		UUID ownerId = DaoUtils.getOwnerId(folder);
		
//		2 ways of getting recent list
//				- for syncing, including moves+deletes
//				- or for actual RSS, including path, no moves+deletes?
//				
//		if( path !)
		recentManager.search(ownerId, folder.getUrl(), new RecentCollector() {

			@Override
			public void process(UUID nameId, UUID updatedById, Date dateModified, String targetHref, String targetName, String updatedByName, RecentResourceType resourceType, RecentActionType actionType, String moveDestHref) {
				if (actionType.equals(RecentActionType.update)) {
					BaseResource r = ClydeUtils.loadResource(nameId);
					if (r != null) {
						appendBaseResource(r, elChannel, includeImages, includePages, includeBinaries);
					}
				}
			}
		});

		elChannel.close().close();

		writer.flush();
	}

	private BaseResourceList getResources() {
		return (BaseResourceList) folder.getPagesRecursive();
	}
	
	
	private void appendBaseResource(BaseResource r, Element elChannel, boolean includeImages, boolean includePages, boolean includeBinaries) {
		if (r == null) {
			return;
		}

		if (r instanceof ImageFile && includeImages) {
			ImageFile img = (ImageFile) r;
			appendImage(img, elChannel);
		} else if (r instanceof Page && includePages) {
			Page page = (Page) r;
			appendPage(page, elChannel);
		} else if (includeBinaries) {
			appendBinaryFile(r, elChannel);
		} else {
			log.trace("Not including: " + r.getClass());
		}

	}

	private void appendPage(Page page, Element elChannel) {
		User creator = page.getCreator();
		String author = null;
		if (creator != null) {
			author = creator.getName();
		}
		Element elItem = elChannel.begin("item").prop("title", page.getTitle()).prop("description", fixHtml(page.getBrief())).prop("link", page.getHref()).prop("pubDate", formatDate(page.getModifiedDate()));

		if (author != null) {
			elItem.prop("author", author);
		}
		elItem.close();
	}

	private void appendBinaryFile(BaseResource page, Element elChannel) {
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

	private void appendImage(ImageFile target, Element elChannel) {
		ImageData data = target.imageData(false);
		Element elImg = elChannel.begin("image").prop("title", target.getTitle()).prop("link", target.getParent().getHref()).prop("pubDate", formatDate(target.getModifiedDate())).prop("url", target.getHref());
		if (data != null) {
			elImg.prop("width", data.getWidth());
			elImg.prop("height", data.getHeight());
		}
		elImg.close(true);
	}
	
}
