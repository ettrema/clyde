package com.bradmcevoy.web.calendar;

import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.TextFile;
import com.ettrema.http.ICalResource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;

/**
 *
 */
@BeanPropertyResource(value="CAL")
public class CalendarEvent extends TextFile implements ICalResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CalendarEvent.class );
    private static final long serialVersionUID = 1L;

    private transient Calendar calendar;

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
        return getContent();
    }

    public void setiCalData( String iCalData ) {
        setContent(iCalData);
    }

    public Calendar getCalendar() {
        if( calendar == null ) {
            CalendarBuilder builder = new CalendarBuilder();
            StringReader sin = new StringReader(getContent());
            try {
                calendar = builder.build(sin);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ParserException ex) {
                throw new RuntimeException(ex);
            }
        }
        return calendar;
    }

    public VEvent getEvent() {
        return (VEvent) getCalendar().getComponent("VEVENT");
    }

    public String getSummary() {
        return getEvent().getSummary().getValue();
    }

    public void setSummary(String s) {
        getEvent().getSummary().setValue(s);
        toData();
    }

    @Override
    public String getTitle() {
        return getSummary();
    }


    public Date getStartDate() {
        long tm = getEvent().getStartDate().getDate().getTime();
        return new Date(tm);
    }

    public void setStartDate(Date d) {
        log.debug("setStartDate: " + d);
        DateTime dt = new DateTime(d.getTime());
        getEvent().getStartDate().setDate(dt);
        log.debug("start date is now: " + getEvent().getEndDate().getValue());
        toData();
    }

    public String getStart() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(getEvent().getStartDate().getDate());
        log.debug("start hour: " + cal.get(java.util.Calendar.HOUR_OF_DAY));
        return DateUtils.formatDate(getStartDate());
    }

    public Date getEndDate() {
        long tm = getEvent().getEndDate().getDate().getTime();
        return new Date(tm);
    }

    public String getEnd() {
        return DateUtils.formatDate(getEndDate());
    }

    public void setEndDate(Date d) {
        log.debug("setStartDate: " + d);
        DateTime icalDate = new DateTime(d.getTime());
        getEvent().getEndDate().setDate(icalDate);
        log.debug("end date is now: " + getEvent().getEndDate().getValue());
        toData();
    }

    public String getDescription() {
        return getEvent().getDescription().getValue();
    }

    public void setDescription(String d) {
        getEvent().getDescription().setValue(d);
        toData();
    }

    private void toData() {
        CalendarOutputter outputter = new CalendarOutputter(false);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            outputter.output(calendar, bout);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }
        setContent(bout.toString());
    }


}
