package com.ettrema.web.calendar;

import com.bradmcevoy.http.ReportableResource;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.CalendarResource;

/**
 *
 */
public class Calendar extends Folder implements CalendarResource, AccessControlledResource, ReportableResource {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Calendar.class );
    private static final long serialVersionUID = 1L;

	private String color;
	
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

	@Override
    public String getCalendarDescription() {
        return this.getTitle();
    }

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public void setColor(String s) {
		this.color = s;
	}


}
