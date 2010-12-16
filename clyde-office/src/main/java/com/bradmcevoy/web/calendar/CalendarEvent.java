package com.bradmcevoy.web.calendar;

import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.calendar.utils.CalendarUtils;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.DateVal;
import com.ettrema.http.ICalResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

/**
 *
 */
@BeanPropertyResource( value = "clyde" )
public class CalendarEvent extends Folder implements ICalResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CalendarEvent.class );
    private static final long serialVersionUID = 1L;

    public CalendarEvent( Folder parentFolder, String name ) {
        super( parentFolder, name );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        CalendarEvent uNew = (CalendarEvent) super.copyInstance( parent, newName );
        return uNew;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new CalendarEvent( parent, newName );
    }

    @Override
    public String getContentType( String accepts ) {
        return "text/calendar";
    }

    public String getICalData() {
        try {
            Calendar cal = getCalendar();
            CalendarOutputter outputter = new CalendarOutputter();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            outputter.output( cal, bout );
            return bout.toString( "UTF-8" );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } catch( ValidationException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public void setiCalData( String iCalData ) {

        ByteArrayInputStream fin = null;
        try {
            fin = new ByteArrayInputStream( iCalData.getBytes( "UTF-8" ) );
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build( fin );
            setCalendar( calendar );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } catch( ParserException ex ) {
            throw new RuntimeException( ex );
        } finally {
            try {
                fin.close();
            } catch( IOException ex ) {
                Logger.getLogger( CalendarEvent.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }

    }

    public Calendar getCalendar() {
        Calendar calendar = new Calendar();
        calendar.getProperties().add( new ProdId( "-//Ben Fortuna//iCal4j 1.0//EN" ) );
        calendar.getProperties().add( Version.VERSION_2_0 );
        calendar.getProperties().add( CalScale.GREGORIAN );

        java.util.Calendar cal = java.util.Calendar.getInstance();
        Date dt = getStartDate();
        cal.setTime( dt );

        VEvent event = new VEvent( new net.fortuna.ical4j.model.Date( cal.getTime() ), getSummary() );
// initialise as an all-day event..
//        christmas.getProperties().getProperty( Property.DTSTART ).getParameters().add( Value.DATE );
        calendar.getComponents().add( event );
        return calendar;
    }

    private void setCalendar( Calendar calendar ) {
        VEvent ev = event( calendar );
        setStartDate( ev.getStartDate().getDate() );
        Date endDate = null;
        if( ev.getEndDate() != null ) {
            endDate = ev.getEndDate().getDate();
        }
        setEndDate( endDate );
        String summary = null;
        if( ev.getSummary() != null ) {
            summary = ev.getSummary().getValue();
        }
        setSummary( summary );
    }

    private VEvent event( Calendar cal ) {
        return (VEvent) cal.getComponent( "VEVENT" );
    }

    public VEvent getEvent() {
        return event( getCalendar() );
    }

    public Date getStartDate() {
        ComponentValue cv = this.getValues().get( "startDate" );
        if( cv == null ) {
            return null;
        } else {
            Object val = cv.getValue();
            Date dt = (Date) val;
            return dt;
        }
    }

    public void setStartDate( Date d ) {
        ComponentValue cv = this.getValues().get( "startDate" );
        if( cv == null ) {
            cv = new DateVal( "startDate", this );
            this.getValues().add( cv );
        }
        cv.setValue( d );
    }

    public String getStart() {
        return CalendarUtils.formatDate( getStartDate() );
    }

    public Date getEndDate() {
        ComponentValue cv = this.getValues().get( "endDate" );
        if( cv == null ) {
            return null;
        } else {
            Object val = cv.getValue();
            return (Date) val;
        }
    }

    public String getEnd() {
        return CalendarUtils.formatDate( getEndDate() );
    }

    public void setEndDate( Date d ) {
        ComponentValue cv = this.getValues().get( "endDate" );
        if( cv == null ) {
            cv = new DateVal( "endDate", this );
            this.getValues().add( cv );
        }
        cv.setValue( d );
    }

    public String getDescription() {
        ComponentValue cv = this.getValues().get( "body" );
        if( cv == null ) {
            return null;
        } else {
            Object val = cv.getValue();
            if( val == null ) {
                return null;
            } else {
                return val.toString();
            }
        }
    }

    public void setDescription( String d ) {
        ComponentValue cv = this.getValues().get( "body" );
        if( cv == null ) {
            cv = new ComponentValue( "body", this );
            this.getValues().add( cv );
        }
        cv.setValue( d );
    }

    public String getSummary() {
        ComponentValue cv = this.getValues().get( "brief" );
        if( cv == null ) {
            return null;
        } else {
            Object val = cv.getValue();
            if( val == null ) {
                return null;
            } else {
                return val.toString();
            }
        }
    }

    public void setSummary( String d ) {
        ComponentValue cv = this.getValues().get( "brief" );
        if( cv == null ) {
            cv = new ComponentValue( "brief", this );
            this.getValues().add( cv );
        }
        cv.setValue( d );
    }


}
