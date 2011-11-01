package com.ettrema.media.dao;

import com.ettrema.media.MediaLogServiceImpl;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author brad
 */
public interface MediaLogCollector {

	void onResult(UUID nameId, Date dateTaken, Double locLat, Double locLong, String mainContentPath, String thumbPath, MediaLogServiceImpl.MediaType type);
	
}
