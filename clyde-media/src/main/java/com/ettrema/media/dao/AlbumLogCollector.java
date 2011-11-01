package com.ettrema.media.dao;

import com.ettrema.media.MediaLogServiceImpl;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author brad
 */
public interface AlbumLogCollector {

	void onResult(UUID nameId, UUID ownerId, Date dateStart, Date endDate, Double locLat, Double locLong, String mainPath, String thumbPath1,String thumbPath2,String thumbPath3, MediaLogServiceImpl.MediaType type);
	
}
