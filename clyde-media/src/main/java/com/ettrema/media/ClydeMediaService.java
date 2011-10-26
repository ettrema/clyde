package com.ettrema.media;

import com.ettrema.web.FlashFileCreator;
import com.ettrema.web.ImageFileCreator;
import com.ettrema.web.VideoFileCreator;
import com.ettrema.web.creation.ResourceCreator;
import com.ettrema.context.Context;
import com.ettrema.context.Factory;
import com.ettrema.context.Registration;
import com.ettrema.context.RootContext;

/**
 *
 * @author brad
 */
public class ClydeMediaService implements Factory{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeMediaService.class);

    public Class[] keyClasses() {
        return null;
    }

    public String[] keyIds() {
        return null;
    }

    public Registration insert( RootContext context, Context requestContext ) {
        throw new UnsupportedOperationException( "not supported by the factory");
    }
    
    public void init( RootContext context ) {
        log.debug( "hello from ClydeMediaService. Registering with the ResourceCreator... ");
        ResourceCreator rc = context.get( ResourceCreator.class);
        rc.addCreator(new ImageFileCreator());
        rc.addCreator(new FlashFileCreator());
        rc.addCreator(new VideoFileCreator());
    }

    public void destroy() {

    }

    public void onRemove( Object item ) {

    }

}
