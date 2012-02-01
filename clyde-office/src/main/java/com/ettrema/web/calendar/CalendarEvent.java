package com.ettrema.web.calendar;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.Formatter;
import com.ettrema.web.calendar.utils.CalendarUtils;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.DateVal;
import com.ettrema.http.ICalResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

/**
 *
 */
@BeanPropertyResource(value = "clyde")
public class CalendarEvent extends Folder implements ICalResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalendarEvent.class);
    private static final long serialVersionUID = 1L;

    public CalendarEvent(Folder parentFolder, String name) {
        super(parentFolder, name);
    }

    @Override
    public Resource child(String name) {
        if (name.equals("event.ics")) {
            return new IcsFile(name);
        } else {
            return super.child(name);
        }
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        CalendarEvent uNew = (CalendarEvent) super.copyInstance(parent, newName);
        return uNew;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new CalendarEvent(parent, newName);
    }

    @Override
    public String getContentType(String accepts) {
        return "text/calendar";
    }

    @Override
    public String getICalData() {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            writeData(bout);
            return bout.toString("UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeData(OutputStream out) {
        try {
            Calendar cal = getCalendar();
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(cal, out);
        } catch (IOException | ValidationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setiCalData(String iCalData) {

        ByteArrayInputStream fin = null;
        try {
            fin = new ByteArrayInputStream(iCalData.getBytes("UTF-8"));
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(fin);
            setCalendar(calendar);
        } catch (IOException | ParserException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                fin.close();
            } catch (IOException ex) {
                Logger.getLogger(CalendarEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public Calendar getCalendar() {

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//ettrema.com//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        String sTimezone = getTimezone();
        TimeZone timezone = null;
        if (sTimezone != null && sTimezone.length() > 0) {
            registry.getTimeZone(sTimezone); // Eg Pacific/Auckland
        }
        if (timezone == null) {
            timezone = registry.getTimeZone("Pacific/Auckland");
            log.warn("Couldnt find timezone: " + sTimezone + ", using default: " + timezone);            
        }
        VTimeZone tz = timezone.getVTimeZone();
        calendar.getComponents().add(tz);
        net.fortuna.ical4j.model.DateTime start = toCalDateTime(getStartDate(), timezone);
        net.fortuna.ical4j.model.DateTime finish = toCalDateTime(getEndDate(), timezone);
        String summary = Formatter.getInstance().toPlain(getSummary());
        VEvent event = new VEvent(start, finish, summary);
        event.getProperties().add(new Uid(this.getId().toString()));
        event.getProperties().add(tz.getTimeZoneId());
        // initialise as an all-day event..
        //        christmas.getProperties().getProperty( Property.DTSTART ).getParameters().add( Value.DATE );
        calendar.getComponents().add(event);
        return calendar;

    }

    private void setCalendar(Calendar calendar) {
        VEvent ev = event(calendar);
        setStartDate(ev.getStartDate().getDate());
        Date endDate = null;
        if (ev.getEndDate() != null) {
            endDate = ev.getEndDate().getDate();
        }
        setEndDate(endDate);
        String summary = null;
        if (ev.getSummary() != null) {
            summary = ev.getSummary().getValue();
        }
        setSummary(summary);
    }

    private VEvent event(Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT");
    }

    public VEvent getEvent() {
        return event(getCalendar());
    }

    public Date getStartDate() {
        ComponentValue cv = this.getValues().get("startDate");
        if (cv == null) {
            return null;
        } else {
            Object val = cv.getValue();
            Date dt = (Date) val;
            return dt;
        }
    }

    public void setStartDate(Date d) {
        ComponentValue cv = this.getValues().get("startDate");
        if (cv == null) {
            cv = new DateVal("startDate", this);
            this.getValues().add(cv);
        }
        cv.setValue(d);
    }

    public String getTimezone() {
        ComponentValue cv = this.getValues().get("timezone");
        if (cv == null || cv.getValue() == null) {
            return null;
        } else {
            Object val = cv.getValue();
            String s = val.toString();
            return s;
        }
    }

    public void setTimezone(String s) {
        ComponentValue cv = this.getValues().get("timezone");
        if (cv == null) {
            cv = new ComponentValue("timezone", this);
            this.getValues().add(cv);
        }
        cv.setValue(s);
    }

    public String getStart() {
        return CalendarUtils.formatDate(getStartDate());
    }

    public Date getEndDate() {
        ComponentValue cv = this.getValues().get("endDate");
        if (cv == null) {
            return null;
        } else {
            Object val = cv.getValue();
            return (Date) val;
        }
    }

    public String getEnd() {
        return CalendarUtils.formatDate(getEndDate());
    }

    public void setEndDate(Date d) {
        ComponentValue cv = this.getValues().get("endDate");
        if (cv == null) {
            cv = new DateVal("endDate", this);
            this.getValues().add(cv);
        }
        cv.setValue(d);
    }

    public String getDescription() {
        ComponentValue cv = this.getValues().get("body");
        if (cv == null) {
            return null;
        } else {
            Object val = cv.getValue();
            if (val == null) {
                return null;
            } else {
                return val.toString();
            }
        }
    }

    public void setDescription(String d) {
        ComponentValue cv = this.getValues().get("body");
        if (cv == null) {
            cv = new ComponentValue("body", this);
            this.getValues().add(cv);
        }
        cv.setValue(d);
    }

    public String getSummary() {
        ComponentValue cv = this.getValues().get("title");
        if (cv == null) {
            return null;
        } else {
            Object val = cv.getValue();
            if (val == null) {
                return null;
            } else {
                String s = val.toString();
                return s;
            }
        }
    }

    public void setSummary(String d) {
        ComponentValue cv = this.getValues().get("title");
        if (cv == null) {
            cv = new ComponentValue("title", this);
            this.getValues().add(cv);
        }
        cv.setValue(d);
    }

    public class IcsFile implements GetableResource, DigestResource {

        private final String name;

        public IcsFile(String name) {
            this.name = name;
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
            writeData(out);
            out.flush();
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        @Override
        public String getContentType(String accepts) {
            return "text/calendar";
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        public String getName() {
            return name;
        }

        public Object authenticate(String user, String password) {
            return CalendarEvent.this.authenticate(user, password);
        }

        public boolean authorise(Request request, Method method, Auth auth) {
            return CalendarEvent.this.authorise(request, method, auth);
        }

        public String getRealm() {
            return CalendarEvent.this.getRealm();
        }

        public Date getModifiedDate() {
            return CalendarEvent.this.getModifiedDate();
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            return CalendarEvent.this.authenticate(digestRequest);
        }

        @Override
        public boolean isDigestAllowed() {
            return CalendarEvent.this.isDigestAllowed();
        }
    }

    private net.fortuna.ical4j.model.Date toCalDate(Date dt, TimeZone timezone) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeZone(timezone);
            cal.setTime(dt);
            net.fortuna.ical4j.model.Date start = new net.fortuna.ical4j.model.Date(toCalDate(cal));
            return start;
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private net.fortuna.ical4j.model.DateTime toCalDateTime(Date dt, TimeZone timezone) {
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeZone(timezone);
            cal.setTime(dt);
            net.fortuna.ical4j.model.DateTime start = new net.fortuna.ical4j.model.DateTime(toCalDateTime(cal));
            return start;
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String toCalDate(java.util.Calendar cal) {
        String s = "";
        s += cal.get(java.util.Calendar.YEAR);
        s += pad2(cal.get(java.util.Calendar.MONTH) + 1);
        s += pad2(cal.get(java.util.Calendar.DAY_OF_MONTH));
        return s;
    }

    private String toCalDateTime(java.util.Calendar cal) {
        String s = toCalDate(cal);
        s += "T";
        s += pad2(cal.get(java.util.Calendar.HOUR_OF_DAY));
        s += pad2(cal.get(java.util.Calendar.MINUTE));
        s += pad2(cal.get(java.util.Calendar.SECOND));
//        s += "Z";
        return s;
        //"20100101T070000Z";
    }

    private String pad2(int i) {
        if (i > 9) {
            return i + "";
        } else {
            return "0" + i;
        }
    }
}
