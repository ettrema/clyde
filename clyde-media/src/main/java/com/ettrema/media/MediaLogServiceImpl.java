package com.ettrema.media;

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
import com.ettrema.media.dao.AlbumLogCollector;
import com.ettrema.media.dao.AlbumLogDao;
import com.ettrema.web.Formatter;
import com.ettrema.web.MusicFile;
import com.ettrema.web.User;
import com.ettrema.web.Web;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class MediaLogServiceImpl implements TableDefinitionSource, EventListener, MediaLogService {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MediaLogServiceImpl.class);
	private final MediaLogDao mediaLogDao;
	private final AlbumLogDao albumLogDao;
	private final ImageService imageService;
	private final ThumbHrefService hrefService;
	private int pageSize = 100;
	private String thumbSuffix = "_sys_thumb";
	private String previewSuffix = "_sys_reg";

	public MediaLogServiceImpl(ImageService imageService, EventManager eventManager, ThumbHrefService hrefService) {
		this(new MediaLogDao(), new AlbumLogDao(), imageService, eventManager, hrefService);
	}

	public MediaLogServiceImpl(MediaLogDao mediaLogDao, AlbumLogDao albumLogDao, ImageService imageService, EventManager eventManager, ThumbHrefService hrefService) {
		this.mediaLogDao = mediaLogDao;
		this.albumLogDao = albumLogDao;
		this.imageService = imageService;
		this.hrefService = hrefService;
		eventManager.registerEventListener(this, LogicalDeleteEvent.class);
		eventManager.registerEventListener(this, PhysicalDeleteEvent.class);
		// will listen to save events, but this is only for logging music files
		// image and video logging is triggered from the ThumbGeneratorService when thumbs are generated
		eventManager.registerEventListener(this, PostSaveEvent.class);
		eventManager.registerEventListener(this, ThumbGeneratedEvent.class);
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
		} else if (e instanceof ThumbGeneratedEvent) {
			ThumbGeneratedEvent tge = (ThumbGeneratedEvent) e;
			if (tge.getResource() instanceof BinaryFile) {
				BinaryFile bf = (BinaryFile) tge.getResource();
				onThumbGenerated(bf);
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

	@Override
	public List<MediaLog> getMedia(BaseResource owner, String folderPath, int page) {
		final List<MediaLog> list = new ArrayList<MediaLog>();
		searchMedia(owner.getNameNodeId(), folderPath, page, new MediaLogCollector() {

			@Override
			public void onResult(UUID nameId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type) {
				list.add(new MediaLog(nameId, dateTaken, locLat, locLong, mainContentPath, thumbPath, type));
			}
		});
		return list;
	}

	@Override
	public List<AlbumLog> getAlbums(BaseResource owner, String folderPath) {
		final List<AlbumLog> list = new ArrayList<AlbumLog>();
		searchAlbums(owner.getNameNodeId(), folderPath, new AlbumLogCollector() {

			@Override
			public void onResult(UUID nameId, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1, String thumbPath2, String thumbPath3, MediaType type) {
				list.add(new AlbumLog(nameId, ownerId, dateStart, endDate, locLat, locLong, mainPath, thumbPath1, thumbPath2, thumbPath3, type));
			}
		});
		return list;
	}

	@Override
	public List<AlbumYear> getAlbumTimeline(BaseResource owner, String folderPath) {
		List<AlbumYear> list = new ArrayList<AlbumYear>();
		List<AlbumLog> logs = getAlbums(owner, folderPath);
		Map<Integer, AlbumYear> byYear = new HashMap<Integer, AlbumYear>();
		Formatter f = Formatter.getInstance();
		for (AlbumLog l : logs) {
			Integer year = f.getYear(l.getDateStart());
			AlbumYear ay = byYear.get(year);
			if (ay == null) {
				ay = new AlbumYear(year);
				byYear.put(year, ay);
				list.add(ay);
			}
			ay.add(l);
		}
		return list;
	}

	/**
	 *
	 * @param ownerId
	 * @param page - zero indexed. Ie 0 = first page
	 * @return - the number of results processed
	 */
	public int searchMedia(UUID ownerId, String folderPath, int page, MediaLogCollector collector) {
		if (log.isTraceEnabled()) {
			log.trace("search: hostId:" + ownerId + " path: " + folderPath);
		}
		int limit = pageSize;
		int offset = page * pageSize;
		return mediaLogDao.searchMedia(ownerId, folderPath, limit, offset, collector);
	}

	public int searchAlbums(UUID ownerId, String folderPath, AlbumLogCollector collector) {
		if (log.isTraceEnabled()) {
			log.trace("search: hostId:" + ownerId + " path: " + folderPath);
		}
		return albumLogDao.search(ownerId, folderPath, 100000, 0, collector);
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
		System.out.println("addImage");
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
				Folder album = file.getParentFolder();
				UUID galleryId = album.getNameNodeId();
				mediaLogDao.createOrUpdateMedia(galleryId, ownerId, file, takenDate, locLat, locLong, previewPath, thumbPath, MediaType.IMAGE);

				// Now update the album
				AlbumLog a = albumLogDao.get(galleryId);
				if (a == null) {
					// create a new one
					System.out.println("create new album");
					a = new AlbumLog(galleryId, ownerId, takenDate, takenDate, locLat, locLong, album.getUrl(), thumbPath, null, null, MediaType.IMAGE);
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
						if (a.getEndDate().before(takenDate)) {
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
