package com.bradmcevoy.web.calendar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import junit.framework.TestCase;
import java.util.Date;
import net.fortuna.ical4j.data.CalendarOutputter;
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
 * @author brad
 */
public class CalendarTest extends TestCase {

    public void test() throws IOException, ValidationException, ParseException {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//ettrema.com//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone("Pacific/Auckland");
        VTimeZone tz = timezone.getVTimeZone();
        calendar.getComponents().add(tz);


        java.util.Calendar cal = java.util.Calendar.getInstance();
        System.out.println("cal1: " + cal);
        System.out.println("cal.time: " + cal.getTime());
        cal.setTimeZone(timezone);
        System.out.println("cal2: " + cal);
        Date dt = new Date();
        System.out.println("dt: " + dt);
        cal.setTime(dt);
        System.out.println("cal3: " + cal);
        System.out.println("cal.time: " + cal.getTime());
       
        net.fortuna.ical4j.model.Date start = new net.fortuna.ical4j.model.Date(toCalDate(cal));
        System.out.println("startA: " + start);

        VEvent event = new VEvent(start, "Summmmmmmart");
        event.getProperties().add(new Uid("X"));
        event.getProperties().add(tz.getTimeZoneId());

        calendar.getComponents().add(event);
        System.out.println("got calendar: " + calendar);
        System.out.println("---------------");
        CalendarOutputter outputter = new CalendarOutputter();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        outputter.output(calendar, bout);
        System.out.println("data: " + bout.toString());
    }

    String toCalDate(java.util.Calendar cal) {
        String s = "";
        s += cal.get(java.util.Calendar.YEAR);
        s += pad2(cal.get(java.util.Calendar.MONTH)+1);
        s += pad2(cal.get(java.util.Calendar.DAY_OF_MONTH));
//        s += "T";
//        s += pad2(cal.get(java.util.Calendar.HOUR_OF_DAY));
//        s += pad2(cal.get(java.util.Calendar.MINUTE));
//        s += pad2(cal.get(java.util.Calendar.SECOND));
//        s += "Z";
        return s;
        //"20100101T070000Z";
    }

    private String pad2(int i) {
        if( i > 9 ) {
            return i + "";
        } else {
            return "0" + i;
        }
    }
}
