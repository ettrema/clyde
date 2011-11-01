package com.ettrema.media;

import com.ettrema.web.BaseResource;
import com.ettrema.web.Formatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Provides access to the media and albums (ie media collections) for particular 
 * "owner" resources. Generally, an owner can either be a Host or a User. Media
 * are only ever aggregated to a single owner.
 *
 * @author bradm
 */
public interface MediaLogService {
	public enum MediaType {
		AUDIO,
		IMAGE,
		VIDEO
	}
	
	/**
	 * Get all albums for the given owner and path
	 * 
	 * @param owner - user or web that owns the media
	 * @param folderPath - optional, limit albums to only within the given path
	 * @return 
	 */
	List<AlbumLog> getAlbums(BaseResource owner, String folderPath);
	
	List<AlbumYear> getAlbumTimeline(BaseResource owner, String folderPath);

	List<MediaLog> getMedia(BaseResource owner, String folderPath, int page);

	public class AlbumYear extends ArrayList<AlbumMonth> {
		private final int year;
		private final Map<Integer,AlbumMonth> byMonth = new HashMap<Integer, AlbumMonth>();

		public AlbumYear(int year) {
			this.year = year;
		}

		public int getYear() {
			return year;
		}		
		
		public void add(AlbumLog log) {
			Integer month = Formatter.getInstance().getMonth(log.getDateStart());
			AlbumMonth am = byMonth.get(month);
			if( am == null ) {
				am = new AlbumMonth(month);
				byMonth.put(month, am);
				this.add(am);
			}
			am.add(log);
		}
	}
	
	public class AlbumMonth extends ArrayList<AlbumLog> {
		private final int month;

		public AlbumMonth(int month) {
			this.month = month;
		}

		public int getMonthNum() {
			return month;
		}
		
		public String getMonthName() {
			return Formatter.getInstance().getMonthName(month);
		}
	}
	
	public class MediaLog {

		private final UUID nameId;
		private Date dateTaken;
		private Double locLat;
		private Double locLong;
		private String mainPath;
		private String thumbPath;
		private final MediaType type;

		public MediaLog(UUID nameId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type) {
			this.nameId = nameId;
			this.dateTaken = dateTaken;
			this.locLat = locLat;
			this.locLong = locLong;
			this.mainPath = mainContentPath;
			this.thumbPath = thumbPath;
			this.type = type;
		}

		/**
		 * @return the nameId
		 */
		public UUID getNameId() {
			return nameId;
		}

		/**
		 * @return the dateTaken
		 */
		public Date getDateTaken() {
			return dateTaken;
		}

		/**
		 * @return the locLat
		 */
		public Double getLocLat() {
			return locLat;
		}

		/**
		 * @return the locLong
		 */
		public Double getLocLong() {
			return locLong;
		}

		/**
		 * @return the mainPath
		 */
		public String getMainPath() {
			return mainPath;
		}

		/**
		 * @return the thumbPath
		 */
		public String getThumbPath() {
			return thumbPath;
		}

		/**
		 * @return the type
		 */
		public MediaType getType() {
			return type;
		}
	}

	public class AlbumLog {

		private final UUID nameId;
		private final UUID ownerId;
		private Date dateStart;
		private Date endDate;
		private Double locLat;
		private Double locLong;
		private String mainPath;
		private String thumbPath1;
		private String thumbPath2;
		private String thumbPath3;
		private final MediaType type;

		public AlbumLog(UUID nameId, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1, String thumbPath2, String thumbPath3, MediaType type) {
			this.nameId = nameId;
			this.ownerId = ownerId;
			this.dateStart = dateStart;
			this.endDate = endDate;
			this.locLat = locLat;
			this.locLong = locLong;
			this.mainPath = mainPath;
			this.thumbPath1 = thumbPath1;
			this.thumbPath2 = thumbPath2;
			this.thumbPath3 = thumbPath3;
			this.type = type;
		}

		/**
		 * @return the nameId
		 */
		public UUID getNameId() {
			return nameId;
		}

		/**
		 * @return the ownerId
		 */
		public UUID getOwnerId() {
			return ownerId;
		}

		/**
		 * @return the dateStart
		 */
		public Date getDateStart() {
			return dateStart;
		}

		/**
		 * @return the endDate
		 */
		public Date getEndDate() {
			return endDate;
		}

		/**
		 * @return the locLat
		 */
		public Double getLocLat() {
			return locLat;
		}

		/**
		 * @return the locLong
		 */
		public Double getLocLong() {
			return locLong;
		}

		/**
		 * @return the mainPath
		 */
		public String getMainPath() {
			return mainPath;
		}

		/**
		 * @return the thumbPath1
		 */
		public String getThumbPath1() {
			return thumbPath1;
		}

		/**
		 * @return the thumbPath2
		 */
		public String getThumbPath2() {
			return thumbPath2;
		}

		/**
		 * @return the thumbPath3
		 */
		public String getThumbPath3() {
			return thumbPath3;
		}

		/**
		 * @return the type
		 */
		public MediaType getType() {
			return type;
		}

		/**
		 * @param dateStart the dateStart to set
		 */
		public void setDateStart(Date dateStart) {
			this.dateStart = dateStart;
		}

		/**
		 * @param endDate the endDate to set
		 */
		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}

		/**
		 * @param locLat the locLat to set
		 */
		public void setLocLat(Double locLat) {
			this.locLat = locLat;
		}

		/**
		 * @param locLong the locLong to set
		 */
		public void setLocLong(Double locLong) {
			this.locLong = locLong;
		}

		/**
		 * @param mainPath the mainPath to set
		 */
		public void setMainPath(String mainPath) {
			this.mainPath = mainPath;
		}

		/**
		 * @param thumbPath1 the thumbPath1 to set
		 */
		public void setThumbPath1(String thumbPath1) {
			this.thumbPath1 = thumbPath1;
		}

		/**
		 * @param thumbPath2 the thumbPath2 to set
		 */
		public void setThumbPath2(String thumbPath2) {
			this.thumbPath2 = thumbPath2;
		}

		/**
		 * @param thumbPath3 the thumbPath3 to set
		 */
		public void setThumbPath3(String thumbPath3) {
			this.thumbPath3 = thumbPath3;
		}
	}
}
