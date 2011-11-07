package com.ettrema.web.recent;

import com.ettrema.utils.LogUtils;
import com.ettrema.vfs.PostgresUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * A "Recent" record is just a record of some action, such a an updated file
 * or moved folder
 * 
 * The RecentDao control persisting and loading these records
 *
 * @author bradm
 */
public class RecentDao {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecentDao.class);

	public enum RecentResourceType {

		folder,
		binary,
		other
	}

	public enum RecentActionType {

		update,
		move,
		delete
	}
	public static final RecentTable recentTable = new RecentTable();
	private final String insertSql;
	private final String searchByOwnerAndPathSql;
	private final String searchByOwnerAndDateSql;
	private final String deleteByNameId;
	private final String deleteByNameIdAndActionType;

	public RecentDao() {
		insertSql = recentTable.getInsert();
		String orderBy = " ORDER BY " + recentTable.dateModified.getName();
		// ordered DESC for most recent first
		searchByOwnerAndPathSql = recentTable.getSelect() + " WHERE " + recentTable.ownerId.getName() + " = ? AND " + recentTable.targetHref.getName() + " LIKE ? || '%' " + orderBy + " DESC";
		
		// ordered ASC by date, so that entries are processed oldest to newest
		searchByOwnerAndDateSql = recentTable.getSelect() + " WHERE " + recentTable.ownerId.getName() + " = ? AND " + recentTable.dateModified.getName() + " > ?" + orderBy;
		
		deleteByNameId = recentTable.getDeleteBy(recentTable.nameId);
		deleteByNameIdAndActionType = deleteByNameId + " AND " + recentTable.actionType.getName() + " = ?";
	}

	public void search(String ownerId, Date since, RecentCollector collector) {
		if(since == null ) {
			throw new IllegalArgumentException("since parameter cannot be null");
		}
			
		try {
			_search(ownerId, null, since, collector);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void search(String ownerId, String path, RecentCollector collector) {
		if(path == null ) {
			throw new IllegalArgumentException("path parameter cannot be null");
		}		
		try {
			_search(ownerId, path, null, collector);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void _search(String ownerId, String path, Date since, RecentCollector collector) throws SQLException {
		LogUtils.trace(log, "_search", ownerId, path, since);
		Connection con = PostgresUtils.con();
		PreparedStatement stmt = null;
		Timestamp tsdateModified;
		Timestamp tsSince;
		try {
			if (since == null) {
				stmt = con.prepareStatement(searchByOwnerAndPathSql);
				stmt.setString(1, ownerId);
				stmt.setString(2, path);
			} else {
				stmt = con.prepareStatement(searchByOwnerAndDateSql);
				stmt.setString(1, ownerId);
				tsSince = new java.sql.Timestamp(since.getTime());
				stmt.setTimestamp(2, tsSince);
				System.out.println("sql: " + searchByOwnerAndDateSql);
				System.out.println("date: " + tsSince);
			}
			ResultSet rs = null;
			try {
				rs = stmt.executeQuery();
				while (rs.next()) {
					UUID nameId = UUID.fromString(recentTable.nameId.get(rs));
					UUID updatedById = UUID.fromString(recentTable.updatedById.get(rs));
					tsdateModified = recentTable.dateModified.get(rs);
					Date dateModified = new Date(tsdateModified.getTime());
					String targetHref = recentTable.targetHref.get(rs);
					String targetName = recentTable.targetName.get(rs);
					String updatedByName = recentTable.updatedByName.get(rs);
					RecentResourceType resourceType = RecentResourceType.valueOf(recentTable.resourceType.get(rs));
					RecentActionType actionType = RecentActionType.valueOf(recentTable.actionType.get(rs));
					String moveDestHref = recentTable.moveDestHref.get(rs);
					collector.process(nameId, updatedById, dateModified, targetHref, targetName, updatedByName, resourceType, actionType, moveDestHref);
				}
			} finally {
				PostgresUtils.close(rs);
			}
		} finally {
			PostgresUtils.close(stmt);
		}
	}

	public void delete(UUID nameId) {
		Connection con = PostgresUtils.con();
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(deleteByNameId);
			// name_uuid,owner_id,updated_by_id,date_modified,target_href,target_name,updated_by_name,resource_type,action_type,move_dest_href
			recentTable.nameId.set(stmt, 1, nameId.toString());
			stmt.execute();
		} catch (SQLException ex) {
			throw new RuntimeException(insertSql, ex);
		} finally {
			PostgresUtils.close(stmt);
		}		
	}
	
	public void delete(UUID nameId, RecentActionType actionType) {
		Connection con = PostgresUtils.con();
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(deleteByNameIdAndActionType);
			// name_uuid,owner_id,updated_by_id,date_modified,target_href,target_name,updated_by_name,resource_type,action_type,move_dest_href
			recentTable.nameId.set(stmt, 1, nameId.toString());
			recentTable.nameId.set(stmt, 2, actionType.toString());
			stmt.execute();
		} catch (SQLException ex) {
			throw new RuntimeException(insertSql, ex);
		} finally {
			PostgresUtils.close(stmt);
		}		
	}	
	
	public void insert(UUID nameId, UUID ownerId, UUID updatedById, Date dateModified, String targetHref, String targetName, String updatedByName, RecentResourceType resourceType, RecentActionType actionType, String moveDestHref) {
		Connection con = PostgresUtils.con();
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(insertSql);
			// name_uuid,owner_id,updated_by_id,date_modified,target_href,target_name,updated_by_name,resource_type,action_type,move_dest_href
			recentTable.nameId.set(stmt, 1, nameId.toString());
			recentTable.ownerId.set(stmt, 2, ownerId.toString());
			recentTable.updatedById.set(stmt, 3, updatedById.toString());
			recentTable.dateModified.set(stmt, 4, new java.sql.Timestamp(dateModified.getTime()));
			recentTable.targetHref.set(stmt, 5, targetHref);
			recentTable.targetName.set(stmt, 6, targetName);
			recentTable.updatedByName.set(stmt, 7, updatedByName);
			recentTable.resourceType.set(stmt, 8, resourceType.name());
			recentTable.actionType.set(stmt, 9, actionType.name());
			recentTable.moveDestHref.set(stmt, 10, moveDestHref);
			stmt.execute();
		} catch (SQLException ex) {
			throw new RuntimeException(insertSql, ex);
		} finally {
			PostgresUtils.close(stmt);
		}
	}
	
}
