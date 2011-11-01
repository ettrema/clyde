package com.ettrema.media.dao;

import com.ettrema.media.MediaLogService;
import com.ettrema.media.MediaLogService.MediaType;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author bradmac
 */
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
	private final MediaLogService.MediaType type;

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
	public MediaLogService.MediaType getType() {
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
