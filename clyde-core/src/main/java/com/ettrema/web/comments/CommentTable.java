package com.ettrema.web.comments;

import com.ettrema.db.Table;
import com.ettrema.db.types.FieldTypes;

/**
 *
 * @author brad
 */
public class CommentTable  extends Table{
	// name node id of the host containing the comment
	public final Table.Field hostId = add("host_uuid", FieldTypes.CHARACTER_VARYING, false);    
	// name node id of the comment
	public final Table.Field nameId = add("name_uuid", FieldTypes.CHARACTER_VARYING, false);
	// date the comment was posted
	public final Table.Field<java.sql.Timestamp> datePosted = add("date_posted", FieldTypes.TIMESTAMP, false);
        // path of the page which contains the comment
	public final Table.Field pagePath = add("page_path", FieldTypes.CHARACTER_VARYING, false);

	public CommentTable() {
		super("comment");
		this.addIndex("host_idx", hostId);
                this.addIndex("date_posted_idx", datePosted);
                this.addIndex("page_path_idx", pagePath);
	}
	
}
