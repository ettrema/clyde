package com.ettrema.web.recent;

import com.ettrema.db.Table;
import com.ettrema.db.types.FieldTypes;

/**
 * De-normalised and non-hierarchial list of modified resources for a host or user
 *
 *
 * @author brad
 */
public class RecentTable extends Table {
	// the name node id of the resource which was affected

	public final Field<String> nameId = add("name_uuid", FieldTypes.CHARACTER_VARYING, false);
	public final Field<String> ownerId = add("owner_id", FieldTypes.CHARACTER_VARYING, false);
	// name node Id of the user who made the change
	public final Field<String> updatedById = add("updated_by_id", FieldTypes.CHARACTER_VARYING, false);
	// date and time image was taken. should be derived from image meta data if possible
	public final Field<java.sql.Timestamp> dateModified = add("date_modified", FieldTypes.TIMESTAMP, false);
	public final Field<String> targetHref = add("target_href", FieldTypes.CHARACTER_VARYING, false);
	public final Field<String> targetName = add("target_name", FieldTypes.CHARACTER_VARYING, false);
	public final Field<String> updatedByName = add("updated_by_name", FieldTypes.CHARACTER_VARYING, false);
	
	/**
	 * binary,folder or null
	 */
	public final Field<String> resourceType = add("resource_type", FieldTypes.CHARACTER_VARYING, false);
	
	public final Field<String> actionType = add("action_type", FieldTypes.CHARACTER_VARYING, false);

	/**
	 * If this is a move action, the targetHref will be that of the resource before the move, and this
	 * field will hold the complete destination href.
	 */
	public final Field<String> moveDestHref = add("move_dest_href", FieldTypes.CHARACTER_VARYING, true);

	public RecentTable() {
		super("recent");
		this.addIndex("ownerId", ownerId);
		this.addIndex("dateModified", dateModified);
	}

}
