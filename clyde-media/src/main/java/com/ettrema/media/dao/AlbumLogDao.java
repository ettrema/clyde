package com.ettrema.media.dao;

import com.ettrema.media.DaoUtils;
import com.ettrema.media.MediaLogService.AlbumLog;
import com.ettrema.media.MediaLogService.MediaType;
import com.ettrema.vfs.PostgresUtils;
import com.ettrema.web.Folder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class AlbumLogDao {
	
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlbumLogDao.class);
	public final static AlbumTable ALBUM_TABLE = new AlbumTable();
	
	public AlbumLog get(UUID nameNodeId) {
		String sql = ALBUM_TABLE.getSelect() + " WHERE " + ALBUM_TABLE.getPk().getName() + " = ?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = PostgresUtils.con().prepareStatement(sql);
			stmt.setString(1, nameNodeId.toString());
			rs = stmt.executeQuery();
			final List<AlbumLog> list = new ArrayList<AlbumLog>();
			processResultSet(rs, new AlbumLogCollector() {

				@Override
				public void onResult(UUID nameId, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1, String thumbPath2, String thumbPath3, MediaType type) {
					AlbumLog a = new AlbumLog(nameId, ownerId, dateStart, endDate, locLat, locLong, mainPath, thumbPath1, thumbPath2, thumbPath3, type);
					list.add(a);
				}
			});
			if( list.isEmpty()) {
				return null;
			} else {
				return list.get(0);
			}
		} catch(SQLException e) {
			throw new RuntimeException(sql, e);
		} finally {
			PostgresUtils.close(stmt);
		}
	}
	
	public void createOrUpdate(Folder file, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1, String thumbPath2, String thumbPath3, MediaType type) {
		System.out.println("createOrUpdate1");
		UUID nameId = file.getNameNodeId();
		deleteByNameId(nameId);
		insert(nameId, ownerId, dateStart, endDate, locLat, locLong, mainPath, thumbPath1, thumbPath2, thumbPath3, type.name());
	}

	public void createOrUpdate(AlbumLog a) {
		System.out.println("createOrUpdate: " + a.getMainPath());
		deleteByNameId(a.getNameId());
		insert(a.getNameId(), a.getOwnerId(), a.getDateStart(), a.getEndDate(), a.getLocLat(), a.getLocLong(), a.getMainPath(), a.getThumbPath1(), a.getThumbPath2(), a.getThumbPath3(), a.getType().name());		
	}
	
	
	public void deleteAllByHostId(UUID hostId) {
		String sql = ALBUM_TABLE.getDeleteBy(ALBUM_TABLE.ownerId);
		try {
			PreparedStatement stmt = PostgresUtils.con().prepareStatement(sql);
			stmt.setString(1, hostId.toString());
			int numRecords = stmt.executeUpdate();
			if (log.isTraceEnabled()) {
				log.trace("deleted: " + numRecords + " for hostId: " + hostId + " - " + sql);
			}
		} catch (SQLException ex) {
			throw new RuntimeException(sql, ex);
		}
	}
	
	public void deleteByNameId(UUID nameId) {
		String sql = ALBUM_TABLE.getDelete();
		try {
			PreparedStatement stmt = PostgresUtils.con().prepareStatement(sql);
			stmt.setString(1, nameId.toString());
			int numRecords = stmt.executeUpdate();
			if (log.isTraceEnabled()) {
				log.trace("deleted: " + numRecords + " for name id: " + nameId + " - " + sql);
			}
		} catch (SQLException ex) {
			throw new RuntimeException(sql, ex);
		}
	}
	
	public int searchMedia(UUID hostId, int limit, int offset, AlbumLogCollector collector) {
		return search(hostId, null, limit, offset, collector);
	}
	
	public int search(UUID hostId, String path, int limit, int offset, AlbumLogCollector collector) {

		// ORder by date descending so newest pics first
		String sql = ALBUM_TABLE.getSelect() + " WHERE " + ALBUM_TABLE.ownerId.getName() + " = ? ";
		if (path != null) {
			log.trace("adding path clause: " + path);
			sql = sql + " AND " + ALBUM_TABLE.contentPath.getName() + " LIKE ? || '%' ";
		}
		sql = sql + " ORDER BY " + ALBUM_TABLE.dateStart.getName() + " DESC LIMIT " + limit + " OFFSET " + offset;
		if (log.isTraceEnabled()) {
			log.trace("search: hostid: " + hostId + " sql: " + sql);
		}
		PreparedStatement stmt = null;
		try {
			stmt = PostgresUtils.con().prepareStatement(sql);
			stmt.setString(1, hostId.toString());
			if (path != null) {
				stmt.setString(2, path);
			}
			
			ResultSet rs = null;
			try {
				long tm = System.currentTimeMillis();
				rs = stmt.executeQuery();
				if (log.isTraceEnabled()) {
					tm = System.currentTimeMillis() - tm;
					log.trace("executed media sql in: " + tm + "ms");
				}
				return processResultSet(rs, collector);
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			} finally {
				PostgresUtils.close(rs);
			}
		} catch (SQLException ex) {
			throw new RuntimeException(sql, ex);
		} finally {
			PostgresUtils.close(stmt);
		}
		
	}

	private int processResultSet(ResultSet rs, AlbumLogCollector collector) throws SQLException {
		int num = 0;
		while (rs.next()) {
			num++;
			UUID nameId = getUUID(rs, 1);
			UUID ownerId = getUUID(rs, 2);
			Date dateStart = rs.getTimestamp(3);
			Date dateEnd = rs.getTimestamp(4);
			Double locLat = DaoUtils.getDouble(rs, 5);
			Double locLong = DaoUtils.getDouble(rs, 6);
			String mainPath = rs.getString(7);
			String thumb1 = rs.getString(8);
			String thumb2 = rs.getString(9);
			String thumb3 = rs.getString(10);
			String sType = rs.getString(11);
			MediaType type = MediaType.valueOf(sType);
			
			collector.onResult(nameId,ownerId, dateStart,dateEnd, locLat, locLong, mainPath, thumb1, thumb2, thumb3, type);
		}
		return num;
	}

	private UUID getUUID(ResultSet rs, int num) throws SQLException {
		UUID nameId = null;
		String sNameId = rs.getString(num);
		try {
			nameId = UUID.fromString(sNameId);
		} catch (java.lang.IllegalArgumentException e) {
			log.warn("invalid UUID in media log: " + sNameId);
		}
		return nameId;
	}
	
	private void insert(UUID nameId, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1, String thumbPath2, String thumbPath3, String type) {
		String sql = ALBUM_TABLE.getInsert();
		try {
			int i = 1;
			PreparedStatement stmt = PostgresUtils.con().prepareStatement(sql);
			stmt.setString(i++, nameId.toString());
			stmt.setString(i++, ownerId.toString());
			stmt.setTimestamp(i++, new java.sql.Timestamp(dateStart.getTime()));
			stmt.setTimestamp(i++, new java.sql.Timestamp(endDate.getTime()));
			DaoUtils.setDouble(stmt, i++, locLat);
			DaoUtils.setDouble(stmt, i++, locLong);
			stmt.setString(i++, mainPath);
			stmt.setString(i++, thumbPath1);
			stmt.setString(i++, thumbPath2);
			stmt.setString(i++, thumbPath3);
			stmt.setString(i++, type);			
			
			stmt.execute();
		} catch (SQLException ex) {
			throw new RuntimeException("nameId:" + nameId + " - " + sql, ex);
		}
	}

}
