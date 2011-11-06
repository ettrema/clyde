package com.ettrema.media;

import com.ettrema.web.FlashFileCreator;
import com.ettrema.web.ImageFileCreator;
import com.ettrema.web.VideoFileCreator;
import com.ettrema.web.creation.ResourceCreator;
import com.ettrema.context.Context;
import com.ettrema.context.Factory;
import com.ettrema.context.Registration;
import com.ettrema.context.RootContext;
import com.ettrema.web.MusicFileCreator;

/**
 *
 * @author brad
 */
public class ClydeMediaService implements Factory{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeMediaService.class);

	@Override
    public Class[] keyClasses() {
        return null;
    }

	@Override
    public String[] keyIds() {
        return null;
    }

	@Override
    public Registration insert( RootContext context, Context requestContext ) {
        throw new UnsupportedOperationException( "not supported by the factory");
    }
    
	@Override
    public void init( RootContext context ) {
        log.debug( "hello from ClydeMediaService. Registering media creators with the ResourceCreator... ");
        ResourceCreator rc = context.get( ResourceCreator.class);
        rc.addCreator(new ImageFileCreator()); 
        rc.addCreator(new FlashFileCreator()); 
        rc.addCreator(new VideoFileCreator()); 
		rc.addCreator(new MusicFileCreator()); 
    }

	@Override
    public void destroy() {

    }

	@Override
    public void onRemove( Object item ) {

    }
}
