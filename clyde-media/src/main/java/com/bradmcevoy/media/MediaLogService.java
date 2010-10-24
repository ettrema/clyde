package com.bradmcevoy.media;

import com.bradmcevoy.event.LogicalDeleteEvent;
import com.bradmcevoy.event.PhysicalDeleteEvent;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.FlashFile;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.VideoFile;
import com.bradmcevoy.web.image.ImageService;
import com.bradmcevoy.web.image.ImageService.ExifData;
import com.bradmcevoy.web.image.ThumbHrefService;
import com.ettrema.db.Table;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author brad
 */
public class MediaLogService implements TableDefinitionSource, EventListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaLogService.class );

    public enum MediaType {

        IMAGE,
        VIDEO
    }
    private final MediaLogDao mediaLogDao;
    private final ImageService imageService;
    private final ThumbHrefService hrefService;
    private int pageSize = 100;
    private String thumbSuffix = "_sys_hero";
    private String previewSuffix = "_sys_slideshow";

    public MediaLogService( ImageService imageService, EventManager eventManager, ThumbHrefService hrefService ) {
        this( new MediaLogDao(), imageService, eventManager, hrefService );
    }

    public MediaLogService( MediaLogDao mediaLogDao, ImageService imageService, EventManager eventManager, ThumbHrefService hrefService ) {
        this.mediaLogDao = mediaLogDao;
        this.imageService = imageService;
        this.hrefService = hrefService;
        eventManager.registerEventListener( this, LogicalDeleteEvent.class );
        eventManager.registerEventListener( this, PhysicalDeleteEvent.class );
    }

    public void onEvent( Event e ) {
        if( e instanceof LogicalDeleteEvent ) {
            onDelete( ( (LogicalDeleteEvent) e ).getResource() );
        } else if( e instanceof PhysicalDeleteEvent ) {
            onDelete( ( (PhysicalDeleteEvent) e ).getResource() );
        }
    }

    private void onDelete( BaseResource resource ) {
        if( resource instanceof Host ) {
            Host h = (Host) resource;
            mediaLogDao.deleteAllByHostId( h.getNameNodeId() );
        } else {
            mediaLogDao.deleteLogByNameId( resource.getNameNodeId() );
        }
    }

    /**
     *
     * @param hostId
     * @param page - zero indexed. Ie 0 = first page
     * @return - the number of results processed
     */
    public int search( UUID hostId, int page, ResultCollector collector ) {
        log.trace( "search: " + hostId );
        int limit = pageSize;
        int offset = page * pageSize;
        return mediaLogDao.search( hostId, limit, offset, collector );
    }

    public interface ResultCollector {

        void onResult( UUID nameId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaType type );
    }

    public List<Table> getTableDefinitions() {
        List<Table> list = new ArrayList<Table>();
        list.add( MediaLogDao.TABLE );
        return list;
    }

    public void onThumbGenerated( BinaryFile file ) {
        if( file instanceof ImageFile ) {
            onThumbGenerated( (ImageFile) file );
        } else if( file instanceof FlashFile ) {
            onThumbGenerated( (FlashFile) file );
        } else if( file instanceof VideoFile ) {
            onThumbGenerated( (VideoFile) file );
        } else {
            log.info( "not logging unsupported type: " + file.getClass() );
        }
    }

    public void onThumbGenerated( FlashFile file ) {
        addFlash( file.getHost().getNameNodeId(), file);
    }

    public void addFlash(UUID hostId, FlashFile file ) {
        log.trace( "onThumbGenerated: flashFile" );
        String thumbPath = getThumbUrl( thumbSuffix, file );
        String contentPath = file.getUrl();
        if( thumbPath != null && contentPath != null ) {
            mediaLogDao.createOrUpdate(hostId, file, file.getCreateDate(), null, null, contentPath, thumbPath, MediaType.VIDEO );
        } else {
            log.debug( "no thumb, or not right type" );
        }
    }

    public void onThumbGenerated( VideoFile file ) {
        addVideo( file.getHost().getNameNodeId(), file);
    }

    public void addVideo(UUID hostId, VideoFile file ) {
        log.trace( "onThumbGenerated: video" );
        String thumbPath = getThumbUrl( thumbSuffix, file );
        if( thumbPath == null ) {
            if( log.isTraceEnabled() ) {
                log.trace( "no thumb for: " + file.getUrl() );
            }
            return;
        }
        String contentPath = file.getStreamingVideoUrl();
        if( contentPath == null ) {
            if(log.isTraceEnabled()) {
                log.trace( "no content path for: " + file.getUrl());
            }
            return ;
        }

        mediaLogDao.createOrUpdate( hostId, file, file.getCreateDate(), null, null, contentPath, thumbPath, MediaType.VIDEO );
    }

    private void onThumbGenerated( ImageFile file ) {
        addImage( file.getHost().getNameNodeId(), file );
    }

    public void addImage(UUID hostId, ImageFile file) {
        InputStream in = null;
        try {
            in = file.getInputStream();
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
                log.trace( "no exif data" );
                locLat = null;
                locLong = null;
                takenDate = file.getCreateDate();
            }
            //String path = file.getUrl();
            String thumbPath = getThumbUrl( thumbSuffix, file );
            String previewPath = getThumbUrl( previewSuffix, file );
            if( thumbPath != null && previewPath != null ) {
                mediaLogDao.createOrUpdate(hostId, file, takenDate, locLat, locLong, previewPath, thumbPath, MediaType.IMAGE );
            } else {
                log.trace( "no thumb, or not right type" );
            }

        } finally {
            IOUtils.closeQuietly( in );
        }
    }

    private String getThumbUrl( String suffix, BinaryFile file ) {
        return hrefService.getThumbPath( file, suffix );
//        if( log.isTraceEnabled() ) {
//            log.trace( "getThumbUrl: " + suffix + " for " + file.getUrl() );
//        }
//        HtmlImage thumb = file.thumb( suffix );
//        if( thumb == null ) {
//            log.trace( "no thumb" );
//            return null;
//        } else if( thumb instanceof BinaryFile ) {
//            BinaryFile bf = (BinaryFile) thumb;
//            String s = bf.getUrl();
//            log.trace( "thumb url: " + s );
//            return s;
//        } else {
//            log.trace( "thumb not right type: " + thumb.getClass() );
//            return null;
//        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize( int pageSize ) {
        this.pageSize = pageSize;
    }
}
