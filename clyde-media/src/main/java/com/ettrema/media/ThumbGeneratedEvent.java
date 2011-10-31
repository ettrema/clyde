package com.ettrema.media;

import com.bradmcevoy.http.Resource;
import com.ettrema.event.ResourceEvent;

/**
 * Event fires when a thumbnail is generated for a resource.
 *
 * @author brad
 */
public class ThumbGeneratedEvent implements ResourceEvent {

	private final Resource resource;

	public ThumbGeneratedEvent(Resource resource) {
		this.resource = resource;
	}
		
	
	@Override
	public Resource getResource() {
		return resource;
	}
	
}
