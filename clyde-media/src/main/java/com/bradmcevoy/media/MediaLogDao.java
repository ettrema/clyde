package com.bradmcevoy.media;

import com.bradmcevoy.media.MediaLogService.MediaType;
import com.bradmcevoy.media.MediaLogService.ResultCollector;
import com.bradmcevoy.web.BaseResource;
import com.ettrema.db.Table;
import com.ettrema.db.Table.Field;
import com.ettrema.vfs.PostgresUtils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class MediaLogDao {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaLogDao.class );
    public final static MediaTable TABLE = new MediaTable();

    public int search( UUID hostId, int limit, int offset, ResultCollector collector ) {

        // ORder by date descending so newest pics first
        String sql = TABLE.getSelect() + " WHERE " + TABLE.hostId.getName() + " = ? ORDER BY " + TABLE.dateTaken.getName() + " DESC LIMIT " + limit + " OFFSET " + offset;
        if( log.isTraceEnabled() ) {
            log.trace( "search: hostid: " + hostId + " sql: " + sql );
        }
        PreparedStatement stmt = null;
        try {
            stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, hostId.toString() );

            ResultSet rs = null;
            try {
                rs = stmt.executeQuery();
                int num = 0;
                while( rs.next() ) {
                    num++;
                    UUID nameId = null;
                    String sNameId = rs.getString( 1 );
                    try {
                        nameId = UUID.fromString( sNameId );
                    } catch( java.lang.IllegalArgumentException e ) {
                        log.warn( "invalid UUID in media log: " + sNameId);
                    }
                    Date dateTaken = rs.getTimestamp( 3 );
                    Double locLat = getDouble( rs, 4 );
                    Double locLong = getDouble( rs, 5 );
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

    public void createOrUpdate( UUID hostId, BaseResource file, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type ) {
        UUID nameId = file.getNameNodeId();
        deleteLogByNameId( nameId );
        insert( nameId, hostId, dateTaken, locLat, locLong, mainContentPath, thumbPath, type.name() );

    }

    private void insert( UUID nameId, UUID hostId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, String type ) {
        log.warn( "insert: " + nameId );
        String sql = TABLE.getInsert();
        try {
            PreparedStatement stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, nameId.toString() );
            stmt.setString( 2, hostId.toString() );
            stmt.setTimestamp( 3, new java.sql.Timestamp( dateTaken.getTime() ) );
            setDouble( stmt, 4, locLat );
            setDouble( stmt, 5, locLong );
            stmt.setString( 6, mainContentPath );
            stmt.setString( 7, type );
            stmt.setString( 8, thumbPath );

            stmt.execute();
        } catch( SQLException ex ) {
            throw new RuntimeException( "nameId:" + nameId + " - " + sql, ex );
        }
    }

    private void setDouble( PreparedStatement stmt, int param, Double d ) throws SQLException {
        if( d == null ) {
            stmt.setNull( param, Types.DOUBLE );
        } else {
            stmt.setDouble( param, d );
        }
    }

    private Double getDouble( ResultSet rs, int i ) throws SQLException {
        Double d = (Double) rs.getObject( i );
        return d;
    }

    public void deleteAllByHostId( UUID hostId ) {
        String sql = TABLE.getDeleteBy( TABLE.hostId );
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

    public void deleteLogByNameId( UUID nameId ) {
        String sql = TABLE.getDelete();
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

    public static class MediaTable extends Table {

        public final Field nameId = add( "name_uuid", "character varying", false );
        public final Field hostId = add( "host_uuid", "character varying", false );
        public final Field dateTaken = add( "date_taken", "timestamp", false );
        public final Field locLat = add( "loc_lat", "double precision", true );
        public final Field locLong = add( "loc_long", "double precision", true );
        public final Field mainContentPath = add( "main_path", "character varying", false );
        public final Field mainContentType = add( "main_type", "character varying", false );
        public final Field thumbPath = add( "thumbPath", "character varying", false );

        public MediaTable() {
            super( "media" );
            this.setPrimaryKey( nameId );
        }
    }
}
