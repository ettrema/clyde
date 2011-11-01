package com.ettrema.media;

import com.ettrema.media.dao.AlbumLog;
import com.ettrema.media.dao.MediaLogCollector;
import com.ettrema.media.dao.MediaLogDao;
import com.ettrema.event.LogicalDeleteEvent;
import com.ettrema.event.PhysicalDeleteEvent;
import com.ettrema.web.BaseResource;
import com.ettrema.web.BinaryFile;
import com.ettrema.web.FlashFile;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.ImageFile;
import com.ettrema.web.VideoFile;
import com.ettrema.web.image.ImageService;
import com.ettrema.web.image.ImageService.ExifData;
import com.ettrema.web.image.ThumbHrefService;
import com.ettrema.db.Table;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.ettrema.event.PostSaveEvent;
import com.ettrema.media.dao.AlbumLogDao;
import com.ettrema.web.MusicFile;
import com.ettrema.web.User;
import com.ettrema.web.Web;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class MediaLogService implements TableDefinitionSource, EventListener {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MediaLogService.class);

	public enum MediaType {

		AUDIO,
		IMAGE,
		VIDEO
	}
	private final MediaLogDao mediaLogDao;
	private final AlbumLogDao albumLogDao;
	private final ImageService imageService;
	private final ThumbHrefService hrefService;
	private int pageSize = 100;
	private String thumbSuffix = "_sys_hero";
	private String previewSuffix = "_sys_slideshow";

	public MediaLogService(ImageService imageService, EventManager eventManager, ThumbHrefService hrefService) {
		this(new MediaLogDao(), new AlbumLogDao(), imageService, eventManager, hrefService);
	}

	public MediaLogService(MediaLogDao mediaLogDao, AlbumLogDao albumLogDao, ImageService imageService, EventManager eventManager, ThumbHrefService hrefService) {
		this.mediaLogDao = mediaLogDao;
		this.albumLogDao = albumLogDao;
		this.imageService = imageService;
		this.hrefService = hrefService;
		eventManager.registerEventListener(this, LogicalDeleteEvent.class);
		eventManager.registerEventListener(this, PhysicalDeleteEvent.class);
		// will listen to save events, but this is only for logging music files
		// image and video logging is triggered from the ThumbGeneratorService when thumbs are generated
		eventManager.registerEventListener(this, PostSaveEvent.class);
	}

	@Override
	public void onEvent(Event e) {
		if (e instanceof LogicalDeleteEvent) {
			onDelete(((LogicalDeleteEvent) e).getResource());
		} else if (e instanceof PhysicalDeleteEvent) {
			onDelete(((PhysicalDeleteEvent) e).getResource());
		} else if (e instanceof PostSaveEvent) {
			PostSaveEvent psw = (PostSaveEvent) e;
			if (psw.getResource() instanceof MusicFile) {
				onMusicFileSaved((MusicFile) psw.getResource());
			}
		}
	}

	private void onDelete(BaseResource resource) {
		if (resource instanceof Host) {
			Host h = (Host) resource;
			mediaLogDao.deleteAllByHostId(h.getNameNodeId());
		} else {
			mediaLogDao.deleteLogByNameId(resource.getNameNodeId());
		}
	}

	/**
	 *
	 * @param hostId
	 * @param page - zero indexed. Ie 0 = first page
	 * @return - the number of results processed
	 */
	public int search(UUID hostId, String folderPath, int page, MediaLogCollector collector) {
		if (log.isTraceEnabled()) {
			log.trace("search: hostId:" + hostId + " path: " + folderPath);
		}
		int limit = pageSize;
		int offset = page * pageSize;
		return mediaLogDao.searchMedia(hostId, folderPath, limit, offset, collector);
	}

	@Override
	public List<Table> getTableDefinitions() {
		List<Table> list = new ArrayList<Table>();
		list.add(MediaLogDao.ALBUM_TABLE);
		list.add(MediaLogDao.MEDIA_TABLE);
		return list;
	}

	private void onMusicFileSaved(MusicFile m) {
		log.trace("onMusicFileSaved");
		UUID ownerId = getOwnerId(m);
		addMusic(ownerId, m);
	}

	public void onThumbGenerated(BinaryFile file) {
		if (file instanceof ImageFile) {
			onThumbGenerated((ImageFile) file);
		} else if (file instanceof FlashFile) {
			onThumbGenerated((FlashFile) file);
		} else if (file instanceof VideoFile) {
			onThumbGenerated((VideoFile) file);
		} else {
			log.info("not logging unsupported type: " + file.getClass());
		}
	}

	public void onThumbGenerated(FlashFile file) {
		UUID hostId = getOwnerId(file);
		addFlash(hostId, file);
	}

	public void addFlash(UUID hostId, FlashFile file) {
		log.trace("onThumbGenerated: flashFile");
		String thumbPath = getThumbUrl(thumbSuffix, file);
		String contentPath = file.getUrl();
		if (thumbPath != null && contentPath != null) {
			UUID galleryId = file.getParentFolder().getNameNodeId();
			mediaLogDao.createOrUpdateMedia(galleryId, hostId, file, file.getCreateDate(), null, null, contentPath, thumbPath, MediaType.VIDEO);
		} else {
			log.debug("no thumb, or not right type");
		}
	}

	public void onThumbGenerated(VideoFile file) {
		UUID hostId = getOwnerId(file);
		addVideo(hostId, file);
	}

	private void onThumbGenerated(ImageFile file) {
		UUID hostId = getOwnerId(file);
		addImage(hostId, file);
	}

	private void addMusic(UUID hostId, MusicFile file) {
		log.trace("addMusic");
		String thumbPath = getThumbUrl(thumbSuffix, file);
		if (thumbPath == null) {
			if (log.isTraceEnabled()) {
				log.trace("no thumb for: " + file.getUrl());
			}
			return;
		}
		String contentPath = file.getUrl();
		if (contentPath == null) {
			if (log.isTraceEnabled()) {
				log.trace("no content path for: " + file.getUrl());
			}
			return;
		}
		UUID galleryId = file.getParentFolder().getNameNodeId();
		mediaLogDao.createOrUpdateMedia(galleryId, hostId, file, file.getCreateDate(), null, null, contentPath, thumbPath, MediaType.AUDIO);
	}

	public void addVideo(UUID hostId, VideoFile file) {
		log.trace("onThumbGenerated: video");
		String thumbPath = getThumbUrl(thumbSuffix, file);
		if (thumbPath == null) {
			if (log.isTraceEnabled()) {
				log.trace("no thumb for: " + file.getUrl());
			}
			return;
		}
		String contentPath = file.getStreamingVideoUrl();
		if (contentPath == null) {
			if (log.isTraceEnabled()) {
				log.trace("no content path for: " + file.getUrl());
			}
			return;
		}
		UUID galleryId = file.getParentFolder().getNameNodeId();
		mediaLogDao.createOrUpdateMedia(galleryId, hostId, file, file.getCreateDate(), null, null, contentPath, thumbPath, MediaType.VIDEO);
	}

	public void addImage(UUID ownerId, ImageFile file) {
		InputStream in = null;
		// try to extract location and date taken info from EXIF
		try {
			in = file.getInputStream();
			ExifData exifData = imageService.getExifData(in, file.getName());
			Date takenDate;
			Double locLat;
			Double locLong;
			if (exifData != null) {
				takenDate = exifData.getDate();
				locLat = exifData.getLocLat();
				locLong = exifData.getLocLong();
				if (takenDate == null) {
					takenDate = file.getCreateDate();
				}
			} else {
				log.trace("no exif data");
				locLat = null;
				locLong = null;
				takenDate = file.getCreateDate();
			}
			//String path = file.getUrl();
			String thumbPath = getThumbUrl(thumbSuffix, file);
			String previewPath = getThumbUrl(previewSuffix, file);
			if (thumbPath != null && previewPath != null) {
				UUID galleryId = file.getParentFolder().getNameNodeId();
				mediaLogDao.createOrUpdateMedia(galleryId, ownerId, file, takenDate, locLat, locLong, previewPath, thumbPath, MediaType.IMAGE);
				
				// Now update the album
				AlbumLog a = albumLogDao.get(galleryId);
				if (a == null) {
					// create a new one
					System.out.println("create new album");
					a = new AlbumLog(galleryId, ownerId, takenDate, takenDate, locLat, locLong, previewPath, thumbPath, null, null, MediaType.IMAGE);
				} else {
					// update thumb path 
					System.out.println("update album");
					if (a.getThumbPath1() == null) {
						a.setThumbPath1(thumbPath);
					} else if (a.getThumbPath2() == null) {
						a.setThumbPath2(thumbPath);
					} else if (a.getThumbPath3() == null) {
						a.setThumbPath3(thumbPath);
					}
				}
				
				// If we have lat long set them on the gallery. Note that we don't
				// have any means of recording multiple locations, so last one wins
				if (locLat != null && locLong != null) {
					a.setLocLat(locLat);
					a.setLocLong(locLong);
				}

				// Adjust start and end dates of the gallery to include the dateTaken
				if (takenDate != null) {
					if (a.getDateStart() != null) {
						if (a.getDateStart().after(takenDate)) {
							a.setDateStart(takenDate);
						}
					} else {
						a.setDateStart(takenDate);
					}
					if (a.getEndDate() != null) {
						a.setEndDate(takenDate);
					} else {
						if( a.getEndDate().before(takenDate)) {
							a.setEndDate(takenDate);
						}
					}
				}
				
				albumLogDao.createOrUpdate(a);
			} else {
				log.trace("no thumb, or not right type");
			}

		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private String getThumbUrl(String suffix, BinaryFile file) {
		return hrefService.getThumbPath(file, suffix);
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public void onCreate(Table t, Connection con) {
	}

	private UUID getOwnerId(BaseResource file) {
		if (file instanceof User) {
			return file.getNameNodeId();
		} else if (file instanceof Web) {
			return file.getNameNodeId();
		} else {
			Folder parent = file.getParent();
			if (parent != null) {
				return getOwnerId(parent);
			} else {
				return null;
			}
		}
	}
}
