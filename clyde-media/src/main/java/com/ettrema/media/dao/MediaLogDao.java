package com.ettrema.media.dao;

import com.ettrema.media.MediaLogService.MediaType;
import com.ettrema.web.BaseResource;
import com.ettrema.media.DaoUtils;
import com.ettrema.vfs.PostgresUtils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class MediaLogDao {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MediaLogDao.class);
    public final static MediaTable MEDIA_TABLE = new MediaTable();
    public final static AlbumTable ALBUM_TABLE = new AlbumTable();

    public int searchMedia(UUID hostId, int limit, int offset, MediaLogCollector collector) {
        return searchMedia(hostId, null, limit, offset, collector);
    }

    public int searchMedia(UUID hostId, String path, int limit, int offset, MediaLogCollector collector) {

        // ORder by date descending so newest pics first
        String sql = MEDIA_TABLE.getSelect() + " WHERE " + MEDIA_TABLE.ownerId.getName() + " = ? ";
        if (path != null) {
            log.trace("adding path clause: " + path);
            sql = sql + " AND " + MEDIA_TABLE.mainContentPath.getName() + " LIKE ? || '%' ";
        }
        sql = sql + " ORDER BY " + MEDIA_TABLE.dateTaken.getName() + " DESC LIMIT " + limit + " OFFSET " + offset;
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
                int num = 0;
                while (rs.next()) {
                    num++;
                    UUID nameId = null;
                    String sNameId = rs.getString(1);
                    try {
                        nameId = UUID.fromString(sNameId);
                    } catch (java.lang.IllegalArgumentException e) {
                        log.warn("invalid UUID in media log: " + sNameId);
                    }
                    Date dateTaken = rs.getTimestamp(3);
                    Double locLat = DaoUtils.getDouble(rs, 4);
                    Double locLong = DaoUtils.getDouble(rs, 5);
                    String mainPath = rs.getString(6);
                    String sType = rs.getString(7);
                    MediaType type = MediaType.valueOf(sType);
                    String thumbPath = rs.getString(8);
                    collector.onResult(nameId, dateTaken, locLat, locLong, mainPath, thumbPath, type);
                }
                return num;
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

    public void createOrUpdateMedia(UUID galleryId, UUID hostId, BaseResource file, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type) {
        UUID nameId = file.getNameNodeId();
        deleteLogByNameId(nameId);
        insertMedia(nameId, galleryId, hostId, dateTaken, locLat, locLong, mainContentPath, thumbPath, type.name());
    }

    public void deleteAllByHostId(UUID hostId) {
        String sql = MEDIA_TABLE.getDeleteBy(MEDIA_TABLE.ownerId);
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

    public void deleteLogByNameId(UUID nameId) {
        String sql = MEDIA_TABLE.getDelete();
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

    private void insertMedia(UUID nameId, UUID galleryId, UUID ownerId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, String type) {
        String sql = MEDIA_TABLE.getInsert();
        try {
            PreparedStatement stmt = PostgresUtils.con().prepareStatement(sql);
            stmt.setString(1, nameId.toString());
            stmt.setString(2, galleryId.toString());
            stmt.setString(3, ownerId.toString());
            stmt.setTimestamp(4, new java.sql.Timestamp(dateTaken.getTime()));
            DaoUtils.setDouble(stmt, 5, locLat);
            DaoUtils.setDouble(stmt, 6, locLong);
            stmt.setString(7, mainContentPath);
            stmt.setString(8, type);
            stmt.setString(9, thumbPath);

            stmt.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("nameId:" + nameId + " - " + sql, ex);
        }
    }
}
