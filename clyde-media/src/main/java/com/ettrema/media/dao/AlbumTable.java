package com.ettrema.media.dao;

import com.ettrema.db.Table;
import com.ettrema.db.types.FieldTypes;

/**
 * This is a de-normalised and non-hierarchial data structure for fast read performance
 * for accessing a list of albums for a user or host.
 *
 * An "album" in this context can be a music album or a images or videos
 */
/**
 *
 * @author brad
 */
public class AlbumTable extends Table {
	// name node id of the folder which this gallery is a summary of
	public final Field nameId = add("name_uuid", FieldTypes.CHARACTER_VARYING, false);
	// name node id of the entity which logically owns the gallery. could be user or host, etc
	public final Field ownerId = add("owner_uuid", FieldTypes.CHARACTER_VARYING, false);
	// earliest date of a pic in the gallery
	public final Field dateStart = add("date_start", FieldTypes.TIMESTAMP, false);
	// latest date of a pic in the gallery
	public final Field dateEnd = add("date_end", FieldTypes.TIMESTAMP, false);
	// sample location of any pic in the gallery
	public final Field locLat = add("loc_lat", FieldTypes.FLOAT8, true);
	// sample location of any pic in the gallery
	public final Field locLong = add("loc_long", FieldTypes.FLOAT8, true);
	// url to the gallery
	public final Field contentPath = add("main_path", FieldTypes.CHARACTER_VARYING, false);
	// sample thumbnail
	public final Field thumbPath1 = add("thumbPath1", FieldTypes.CHARACTER_VARYING, true);
	// sample thumbnail
	public final Field thumbPath2 = add("thumbPath2", FieldTypes.CHARACTER_VARYING, true);
	// sample thumbnail
	public final Field thumbPath3 = add("thumbPath3", FieldTypes.CHARACTER_VARYING, true);
	
	public final Field mainContentType = add("main_type", FieldTypes.CHARACTER_VARYING, false);

	public AlbumTable() {
		super("album");
		this.setPrimaryKey(nameId);
	}
	
}
