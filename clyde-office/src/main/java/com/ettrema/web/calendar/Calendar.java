package com.bradmcevoy.web.calendar;

import com.bradmcevoy.http.ReportableResource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.CalendarResource;

/**
 *
 */
public class Calendar extends Folder implements CalendarResource, AccessControlledResource, ReportableResource {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Calendar.class );
    private static final long serialVersionUID = 1L;

    public Calendar( Folder parentFolder, String name ) {
        super( parentFolder, name );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        Calendar uNew = (Calendar) super.copyInstance( parent, newName );
        return uNew;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Calendar( parent, newName );
    }

    public String getCalendarDescription() {
        return this.getTitle();
    }


}
