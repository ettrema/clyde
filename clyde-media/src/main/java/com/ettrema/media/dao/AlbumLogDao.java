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
public class AlbumLogDao {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( AlbumLogDao.class );
	public final static AlbumTable ALBUM_TABLE = new AlbumTable();


	
	public void createOrUpdate(BaseResource file, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1,String thumbPath2,String thumbPath3, MediaType type ) {
        UUID nameId = file.getNameNodeId();
        deleteByNameId( nameId );
        insert( nameId, ownerId, dateStart, endDate, locLat, locLong, mainPath, thumbPath1, thumbPath2, thumbPath3, type.name());
    }


    public void deleteAllByHostId( UUID hostId ) {
        String sql = ALBUM_TABLE.getDeleteBy( ALBUM_TABLE.ownerId );
        try {
            PreparedStatement stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, hostId.toString() );
            int numRecords = stmt.executeUpdate();
            if( log.isTraceEnabled() ) {
                log.trace( "deleted: " + numRecords + " for hostId: " + hostId + " - " + sql );
            }
        } catch( SQLException ex ) {
            throw new RuntimeException( sql, ex );
        }
    }

    public void deleteByNameId( UUID nameId ) {
        String sql = ALBUM_TABLE.getDelete();
        try {
            PreparedStatement stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, nameId.toString() );
            int numRecords = stmt.executeUpdate();
            if( log.isTraceEnabled() ) {
                log.trace( "deleted: " + numRecords + " for name id: " + nameId + " - " + sql );
            }
        } catch( SQLException ex ) {
            throw new RuntimeException( sql, ex );
        }
    }
	
    public int searchMedia( UUID hostId, int limit, int offset, MediaLogCollector collector ) {
        return searchMedia( hostId, null, limit, offset, collector );
    }

    public int searchMedia( UUID hostId, String path, int limit, int offset, MediaLogCollector collector ) {

        // ORder by date descending so newest pics first
        String sql = ALBUM_TABLE.getSelect() + " WHERE " + ALBUM_TABLE.ownerId.getName() + " = ? ";
        if( path != null ) {
            log.trace( "adding path clause: " + path );
            sql = sql + " AND " + ALBUM_TABLE.contentPath.getName() + " LIKE ? || '%' ";
        }
        sql = sql + " ORDER BY " + ALBUM_TABLE.dateStart.getName() + " DESC LIMIT " + limit + " OFFSET " + offset;
        if( log.isTraceEnabled() ) {
            log.trace( "search: hostid: " + hostId + " sql: " + sql );
        }
        PreparedStatement stmt = null;
        try {
            stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, hostId.toString() );
            if( path != null ) {
                stmt.setString( 2, path );
            }

            ResultSet rs = null;
            try {
                long tm = System.currentTimeMillis();
                rs = stmt.executeQuery();
                if( log.isTraceEnabled() ) {
                    tm = System.currentTimeMillis() - tm;
                    log.trace("executed media sql in: " + tm + "ms");
                }
                int num = 0;
                while( rs.next() ) {
                    num++;
                    UUID nameId = null;
                    String sNameId = rs.getString( 1 );
                    try {
                        nameId = UUID.fromString( sNameId );
                    } catch( java.lang.IllegalArgumentException e ) {
                        log.warn( "invalid UUID in media log: " + sNameId );
                    }
                    Date dateTaken = rs.getTimestamp( 3 );
                    Double locLat = DaoUtils.getDouble( rs, 4 );
                    Double locLong = DaoUtils.getDouble( rs, 5 );
                    String mainPath = rs.getString( 6 );
                    String sType = rs.getString( 7 );
                    MediaType type = MediaType.valueOf( sType );
                    String thumbPath = rs.getString( 8 );
                    collector.onResult( nameId, dateTaken, locLat, locLong, mainPath, thumbPath, type );
                }
                return num;
            } catch( SQLException ex ) {
                throw new RuntimeException( ex );
            } finally {
                PostgresUtils.close( rs );
            }
        } catch( SQLException ex ) {
            throw new RuntimeException( sql, ex );
        } finally {
            PostgresUtils.close( stmt );
        }

    }	

	
    private void insert( UUID nameId, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1,String thumbPath2,String thumbPath3, String type ) {
        String sql = ALBUM_TABLE.getInsert();
        try {
			int i = 1;
            PreparedStatement stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( i++, nameId.toString() );
            stmt.setString( i++, ownerId.toString() );
            stmt.setTimestamp( i++, new java.sql.Timestamp( dateStart.getTime() ) );
			stmt.setTimestamp( i++, new java.sql.Timestamp( endDate.getTime() ) );
            DaoUtils.setDouble( stmt, i++, locLat );
            DaoUtils.setDouble( stmt, i++, locLong );
            stmt.setString( i++, mainPath );
            stmt.setString( i++, thumbPath1 );
			stmt.setString( i++, thumbPath2 );
			stmt.setString( i++, thumbPath3 );
            stmt.setString( i++, type );			

            stmt.execute();
        } catch( SQLException ex ) {
            throw new RuntimeException( "nameId:" + nameId + " - " + sql, ex );
        }
    }
}
