package com.bradmcevoy.media;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.HtmlImage;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.image.ImageService;
import com.bradmcevoy.web.image.ImageService.ExifData;
import com.ettrema.db.Table;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.vfs.PostgresUtils;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class MediaLogService implements TableDefinitionSource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaLogService.class );

    public enum MediaType {

        IMAGE,
        VIDEO
    }
    public final static MediaTable MEDIA_TABLE = new MediaTable();
    private final ImageService imageService;
    private int pageSize = 100;

    public MediaLogService( ImageService imageService ) {
        this.imageService = imageService;
    }

    /**
     *
     * @param hostId
     * @param page - zero indexed. Ie 0 = first page
     * @return - the number of results processed
     */
    public int search( UUID hostId, int page, ResultCollector collector ) {
        log.trace( "search: " + hostId);
        int limit = pageSize;
        int offset = page * pageSize;
        String sql = MEDIA_TABLE.getSelect() + " WHERE " + MEDIA_TABLE.hostId.getName() + " = ? LIMIT " + limit + " OFFSET " + offset;
        log.debug( "sql: " + sql);
        PreparedStatement stmt = null;
        try {
            stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, hostId.toString() );

            ResultSet rs = null;
            try {
                rs = stmt.executeQuery();
                int num = 0;
                while( rs.next() ) {
                    log.debug( "rs.next");
                    num++;
                    UUID nameId = UUID.fromString( rs.getString( 1 ) );
                    Date dateTaken = rs.getTimestamp( 3 );
                    Double locLat = getDouble( rs, 4 );
                    Double locLong = getDouble( rs, 5 );
                    String mainPath = rs.getString( 6 );
                    String thumbPath = rs.getString( 7 );
                    String sType = rs.getString( 8 );
                    MediaType type = MediaType.valueOf( sType );
                    log.trace( "on result");
                    collector.onResult( nameId, dateTaken, locLat, locLong, mainPath, thumbPath, type );
                }
                log.debug( "finished");
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

    public interface ResultCollector {

        void onResult( UUID nameId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type );
    }

    public List<Table> getTableDefinitions() {
        List<Table> list = new ArrayList<Table>();
        list.add( MEDIA_TABLE );
        return list;
    }

    public void onThumbGenerated( ImageFile file ) {
        log.warn( "onThumbGenerated" );
        InputStream in = null;
        try {
            in = file.getInputStream();
            log.warn( "check exif" );
            ExifData exifData = imageService.getExifData( in, file.getName() );
            Date takenDate;
            Double locLat;
            Double locLong;
            if( exifData != null ) {
                takenDate = exifData.getDate();
                locLat = exifData.getLocLat();
                locLong = exifData.getLocLong();
                if( takenDate == null ) {
                    takenDate = file.getCreateDate();
                }
            } else {
                log.warn( "no exif data" );
                locLat = null;
                locLong = null;
                takenDate = file.getCreateDate();
            }
            String path = file.getUrl();
            HtmlImage thumb = file.getThumb();
            if( thumb != null && thumb instanceof BinaryFile ) {
                BinaryFile bfThumb = (BinaryFile) thumb;
                String thumbPath = bfThumb.getUrl();
                log.warn( "create log" );
                createOrUpdate( file, takenDate, locLat, locLong, path, thumbPath, MediaType.IMAGE );
            } else {
                log.warn( "no thumb, or not right type" );
            }

        } finally {
            IOUtils.closeQuietly( in );
        }
    }

    public void createOrUpdate( BaseResource file, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type ) {
        UUID nameId = file.getNameNodeId();
        UUID hostId = file.getHost().getNameNodeId();
        deleteIfExists( nameId );
        insert( nameId, hostId, dateTaken, locLat, locLong, mainContentPath, thumbPath, type.name() );

    }

    private void deleteIfExists( UUID nameId ) {
        String sql = MEDIA_TABLE.getDelete();
        try {
            PreparedStatement stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, nameId.toString() );
            int numRecords = stmt.executeUpdate();
            log.warn("deleted: " + numRecords + " for name id: " + nameId + " - " + sql);
        } catch( SQLException ex ) {
            throw new RuntimeException( sql, ex );
        }
    }

    private void insert( UUID nameId, UUID hostId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, String type ) {
        log.warn( "insert: " + nameId);
        String sql = MEDIA_TABLE.getInsert();
        try {
            PreparedStatement stmt = PostgresUtils.con().prepareStatement( sql );
            stmt.setString( 1, nameId.toString() );
            stmt.setString( 2, hostId.toString() );
            stmt.setTimestamp( 3, new java.sql.Timestamp( dateTaken.getTime() ) );
            setDouble( stmt, 4, locLat );
            setDouble( stmt, 5, locLong );
            stmt.setString( 6, mainContentPath );
            stmt.setString( 7, thumbPath );
            stmt.setString( 8, type );

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

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize( int pageSize ) {
        this.pageSize = pageSize;
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
