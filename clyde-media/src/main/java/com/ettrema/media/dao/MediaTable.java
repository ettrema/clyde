package com.ettrema.media.dao;

import com.ettrema.db.Table;
import com.ettrema.db.types.FieldTypes;

/**
 * De-normalised and non-hierarchial list of media files for a user or host
 *
 */
/**
 *
 * @author brad
 */
public class MediaTable extends Table {
	// the name node id of the file which this is a summary of
	public final Field nameId = add("name_uuid", FieldTypes.CHARACTER_VARYING, false);
	// name node Id of the gallery folder which contains the media item
	public final Field albumId = add("gallery_uuid", FieldTypes.CHARACTER_VARYING, false);
	// name node id of the host or user which owns the file
	public final Field ownerId = add("owner_uuid", FieldTypes.CHARACTER_VARYING, false);
	// date and time image was taken. should be derived from image meta data if possible
	public final Field dateTaken = add("date_taken", FieldTypes.TIMESTAMP, false);
	// location of the image, from geo-code meta data
	public final Field locLat = add("loc_lat", FieldTypes.FLOAT8, true);
	// location of the image, from geo-code meta data
	public final Field locLong = add("loc_long", FieldTypes.FLOAT8, true);
	// url of the full resolution media file
	public final Field mainContentPath = add("main_path", FieldTypes.CHARACTER_VARYING, false);
	// content type of the original, full-res media.
	public final Field mainContentType = add("main_type", FieldTypes.CHARACTER_VARYING, false);
	// path to the thumbnail
	public final Field thumbPath = add("thumbPath", FieldTypes.CHARACTER_VARYING, false);

	public MediaTable() {
		super("media");
		this.setPrimaryKey(nameId);
	}
	
}
