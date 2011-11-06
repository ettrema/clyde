package com.ettrema.media;

import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.User;
import com.ettrema.web.Web;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class DaoUtils {

	public static  void setDouble(PreparedStatement stmt, int param, Double d) throws SQLException {
		if (d == null) {
			stmt.setNull(param, Types.DOUBLE);
		} else {
			stmt.setDouble(param, d);
		}
	}

	public static  Double getDouble(ResultSet rs, int i) throws SQLException {
		Double d = (Double) rs.getObject(i);
		return d;
	}
	
	public static  UUID getOwnerId(BaseResource file) {
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
