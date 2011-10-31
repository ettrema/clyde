package com.ettrema.media;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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
}
