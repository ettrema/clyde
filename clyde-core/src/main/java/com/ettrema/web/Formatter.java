package com.ettrema.web;

import com.ettrema.utils.CurrentDateService;
import com.bradmcevoy.utils.FileUtils;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.DateDef;
import com.ettrema.web.component.DateVal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import static com.ettrema.context.RequestContext._;
import org.joda.time.Interval;

/**
 * Handy functions exposes to rendering logic for formatting.
 *
 * @author brad
 */
public class Formatter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Formatter.class);
    public static ThreadLocal<DateFormat> tlSdfUkShort = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy");
        }
    };
    public static ThreadLocal<DateFormat> tlSdfUkLong = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("dd MMMM yyyy");
        }
    };

    public static Formatter getInstance() {
        return _(Formatter.class);
    }
    private final CurrentDateService currentDateService;

    public Formatter(CurrentDateService currentDateService) {
        this.currentDateService = currentDateService;
    }

    /**
     * Null safe method, returns empty string if the value is null
     *
     * @param o
     * @return
     */
    public String toString(Object o) {
        if (o == null) {
            return "";
        } else {
            return o.toString();
        }
    }

    public Boolean toBool(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            return i.intValue() == 0;
        } else if (o instanceof String) {
            String s = (String) o;
            s = s.toLowerCase();
            s = s.trim();
            if (s.length() > 0) {
                return s.equals("true") || s.equals("yes");
            } else {
                return null;
            }
        } else if (o instanceof ComponentValue) {
            ComponentValue cv = (ComponentValue) o;
            return toBool(cv.getValue());
        } else {
            throw new RuntimeException("Unsupported boolean type: " + o.getClass());
        }

    }

    public BigDecimal toDecimal(Object o, int places) {
        if (o == null) {
            return BigDecimal.ZERO;
        } else if (o instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) o;
            return bd.setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof Double) {
            Double d = (Double) o;
            return BigDecimal.valueOf(d).setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            return BigDecimal.valueOf(i.longValue()).setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof Float) {
            Float f = (Float) o;
            return BigDecimal.valueOf(f.doubleValue()).setScale(places, RoundingMode.HALF_UP);
        } else if (o instanceof String) {
            String s = (String) o;
            s = s.trim();
            if (s.length() == 0) {
                return BigDecimal.ZERO;
            } else {
                try {
                    return new BigDecimal(s).setScale(places, RoundingMode.HALF_UP);
                } catch (NumberFormatException numberFormatException) {
                    throw new RuntimeException("Non-numeric data: " + s);
                }
            }
        } else if (o instanceof ComponentValue) {
            ComponentValue cv = (ComponentValue) o;
            return toDecimal(cv.getValue(), places);
        } else {
            throw new RuntimeException("Unsupported value type, should be numeric: " + o.getClass());
        }
    }

    public Double toDouble(Object o) {
        if (o == null) {
            return 0d;
        } else if (o instanceof String) {
            String s = (String) o;
            s = s.trim();
            if (s.length() == 0) {
                return 0d;
            } else {
                try {
                    return Double.valueOf(s);
                } catch (NumberFormatException numberFormatException) {
                    throw new RuntimeException("Non-numeric data: " + s);
                }
            }
        } else if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Integer) {
            Integer i = (Integer) o;
            return (double) i;
        } else if (o instanceof Float) {
            Float f = (Float) o;
            return f.doubleValue();
        } else if (o instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) o;
            return bd.doubleValue();
        } else if (o instanceof ComponentValue) {
            ComponentValue cv = (ComponentValue) o;
            return toDouble(cv.getValue());
        } else {
            throw new RuntimeException("Unsupported value type, should be numeric: " + o.getClass());
        }
    }

    public Long toLong(Object oLimit) {
        return toLong(oLimit, false);
    }

    public Long toLong(Object oVal, boolean withNulls) {
        Long limit;
        if (oVal == null) {
            limit = withNulls ? null : 0l;
        } else if (oVal instanceof Long) {
            limit = (Long) oVal;
        } else if (oVal instanceof Integer) {
            int i = (Integer) oVal;
            limit = (long) i;
        } else if (oVal instanceof Double) {
            Double d = (Double) oVal;
            return d.longValue();
        } else if (oVal instanceof Float) {
            Float d = (Float) oVal;
            return d.longValue();
        } else if (oVal instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) oVal;
            return bd.longValue();
        } else if (oVal instanceof Boolean) {
            Boolean bb = (Boolean) oVal;
            return bb ? 1l : 0l;
        } else if (oVal instanceof String) {
            String s = (String) oVal;
            if (s.length() == 0) {
                limit = withNulls ? null : 0l;
            } else {
                if (s.equals("true") || s.equals("false")) {
                    Boolean b = Boolean.parseBoolean(s);
                    return toLong(b);
                } else {
                    if (s.contains(".")) {
                        Double d = toDouble(s);
                        limit = d.longValue();
                    } else {
                        limit = Long.parseLong(s);
                    }
                }
            }
        } else if (oVal instanceof ComponentValue) {
            ComponentValue cv = (ComponentValue) oVal;
            limit = toLong(cv.getValue());
        } else {
            throw new RuntimeException("unsupported class: " + oVal.getClass());
        }
        return limit;
    }

    public int getYear(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.YEAR);
    }

    public int getMonth(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.MONTH) + 1;
    }

    public int getDayOfMonth(Object o) {
        if (o == null || !(o instanceof Date)) {
            return 0;
        }
        Date dt = (Date) o;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.DAY_OF_MONTH) + 1;
    }

    public String formatDate(Object o) {
        if (o == null) {
            return "";
        } else if (o instanceof Date) {
            return tlSdfUkShort.get().format(o);
        } else if (o instanceof ComponentValue) {
            DateVal dv = (DateVal) o;
            return formatDate(dv.getValue());
        } else if (o instanceof String) {
            return (String) o;
        } else if (o instanceof DateTime) {
            return formatDate(((DateTime) o).toDate());
        } else {
            throw new RuntimeException("Unsupported type: " + o.getClass());
        }
    }

    public String formatDateLong(Object o) {
        if (o == null) {
            return "";
        } else if (o instanceof Date) {
            return tlSdfUkLong.get().format(o);
        } else if (o instanceof ComponentValue) {
            DateVal dv = (DateVal) o;
            return formatDate(dv.getValue());
        } else if (o instanceof String) {
            return (String) o;
        } else {
            throw new RuntimeException("Unsupported type: " + o.getClass());
        }
    }

    /**
     * Returns a user friendly description of the age of the date. Eg "4 minutes
     * ago"
     *
     * @param o
     * @return
     */
    public String formatAge(Object o) {
        org.joda.time.DateTime dt = getDateTime(o);
        DateTime now = new DateTime();
        Interval i = new Interval(dt, now);
        Duration d = i.toDuration();
        long secs = d.getStandardSeconds();
        if (secs < 10) {
            return "Just now";
        } else if (secs < 60) {
            return secs + " seconds ago";
        } else if (secs < 60 * 60) {
            return secs / 60 + " minutes ago";
        } else if (secs < 24 * 60 * 60) {
            return secs / (60 * 60) + " hours ago";
        } else {
            long days = secs/(60*60*24);
            if( days < 2) {
                return "a day ago";
            } else if( days < 30) {
                return days + " days ago";
            } else if( days < 40) {
                return "a month ago";
            } else if( days < 7*8) {
                return days/7 + " weeks ago";
            } else {
                return days/30 + " months ago";
            }
        }
    }

    public org.joda.time.DateTime getDateTime(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof ComponentValue) {
            ComponentValue cv = (ComponentValue) o;
            return getDateTime(cv.getValue());
        } else if (o instanceof String) {
            if (o.toString().length() == 0) {
                return null;
            } else {
                try {
                    Date dt = tlSdfUkShort.get().parse(o.toString());
                    return new DateTime(dt.getTime());
                } catch (ParseException ex) {
                    throw new RuntimeException("Couldnt convert to date: " + o, ex);
                }
            }
        }
        return new DateTime(o);
    }

    /**
     * Format as a percentage, including a percentage symbol and where
     * blank/null values result in a blank output
     *
     * @param num - the numerator
     * @param div - the divisor
     * @return
     */
    public String toPercent(Object num, Object div) {
        return toPercent(num, div, true, true);
    }

    /**
     *
     * @param num
     * @param div
     * @param appendSymbol - if true the percentage symbol is appended if a
     * non-blank value
     * @param withBlanks - if true, blank numerators or divisors result in a
     * blank value. Otherwise return zero.
     * @return
     */
    public String toPercent(Object num, Object div, boolean appendSymbol, boolean withBlanks) {
        Long lNum = toLong(num, true);
        Long lDiv = toLong(div, true);
        if (lDiv == null || lDiv == 0 || lNum == null) {
            if (withBlanks) {
                return "";
            } else {
                return "0" + (appendSymbol ? "%" : "");
            }
        } else {
            long perc = lNum * 100 / lDiv;
            return perc + (appendSymbol ? "%" : "");
        }
    }

    public String format(Object o) {
        if (o == null) {
            return "";
        } else if (o instanceof Date) {
            return formatDate(o);
        } else {
            return o.toString().trim();
        }
    }

    /**
     * Removes the file extension if present
     *
     * Eg file1.swf -> file1
     *
     * file1 -> file1
     *
     * @param s
     * @return
     */
    public String stripExt(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        return FileUtils.stripExtension(s);
    }

    /**
     * True if val1 is greater then val2
     *
     * will do string conversions
     *
     * @param val1
     * @param val2
     * @return
     */
    public boolean gt(Object val1, Object val2) {
        if (val1 == null) {
            return false;
        }
        if (val2 == null) {
            return true;
        }
        Double d1 = toDouble(val1);
        Double d2 = toDouble(val2);
        return d1.doubleValue() > d2.doubleValue();
    }

    public boolean lt(Object val1, Object val2) {
        if (val1 == null) {
            return false;
        }
        if (val2 == null) {
            return true;
        }
        Double d1 = toDouble(val1);
        Double d2 = toDouble(val2);
        return d1.doubleValue() < d2.doubleValue();
    }

    public boolean eq(Object val1, Object val2) {
        if (val1 == null) {
            return (val2 == null);
        }
        if (val2 == null) {
            return false;
        }
        Double d1 = toDouble(val1);
        Double d2 = toDouble(val2);
        return d1.doubleValue() == d2.doubleValue();
    }

    public String htmlEncode(String s) {
        return ComponentUtils.encodeHTML(s);
    }

    public String htmlEncode(ComponentValue cv) {
        String s = cv.toString();
        return htmlEncode(s);
    }

    /**
     * Returns true if the given value is between the start and finish dates, or
     * the respective values are null. Ie if start date is null and finish date
     * is given it will only check that the value is less then the finish date
     *
     * Values are converted using the joda time converters
     *
     * @param oVal
     * @param oStart
     * @param oFinish
     * @return
     */
    public boolean between(Object oVal, Object oStart, Object oFinish) {
        DateTime val = getDateTime(oVal);
        if (val == null) {
            log.warn("null date value");
            return false;
        }
        DateTime start = getDateTime(oStart);
        DateTime finish = getDateTime(oFinish);
        if (start != null) {
            if (val.isBefore(start)) {
                return false;
            }
        }
        if (finish != null) {
            if (val.isAfter(finish)) {
                return false;
            }
        }
        return true;
    }

    public Date toDate(Object oVal) {
        if (oVal == null) {
            return null;
        } else if (oVal instanceof Date) {
            return (Date) oVal;
        } else if (oVal instanceof ComponentValue) {
            ComponentValue cv = (ComponentValue) oVal;
            return toDate(cv.getValue());
        } else {
            if (oVal instanceof String) {
                String s = (String) oVal;
                return DateDef.parseValue(s);
            } else {
                return null;
            }
        }
    }

    public java.sql.Date toSqlDate(Object oVal) {
        Date dt = toDate(oVal);
        if (dt == null) {
            return null;
        } else {
            return new java.sql.Date(dt.getTime());
        }
    }

    public java.sql.Timestamp toSqlTimestamp(Object oVal) {
        Date dt = toDate(oVal);
        if (dt == null) {
            return null;
        } else {
            return new java.sql.Timestamp(dt.getTime());
        }
    }

    public org.joda.time.DateTime toJodaDate(Object oVal) {
        Date dt = toDate(oVal);
        if (dt != null) {
            return new DateTime(dt.getTime());
        } else {
            return null;
        }
    }

    public String toPlain(String html) {
        if (html == null) {
            return null;
        }
        html = replaceTag("br", html, "", "\n");
        html = replaceTag("p", html, "", "\n");
        html = replaceTag("b", html, "", "");
        html = replaceTag("i", html, "", "");
        html = replaceTag("h1", html, "", "");
        html = replaceTag("h2", html, "", "");
        html = replaceTag("h3", html, "", "");
        return html;
    }

    private String replaceTag(String tag, String html, String replaceWithOpening, String replaceWithClosing) {
        html = html.replace("<" + tag + "/>", replaceWithClosing); // self closing
        html = html.replace("<" + tag + ">", replaceWithOpening);  // opening tag
        html = html.replace("</" + tag + ">", replaceWithClosing); // closing tag
        return html;
    }

    public Date getNow() {
        return currentDateService.getNow();
    }

    /**
     * Get the duration from the start to the finish date in seconds.
     *
     * @param start - any object which can be converted to a jodadate
     * @param finish - any object which can be converted to a jodadate
     * @return
     */
    public long durationSecs(Object start, Object finish) {
        DateTime jodaSt = toJodaDate(start);
        DateTime jodaFn = toJodaDate(finish);
        Duration d = new Duration(jodaSt, jodaFn);
        return d.getStandardSeconds();
    }

    public String getMonthName(int i) {
        switch (i) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
            default:
                return "Unknown month " + i;
        }
    }

    public CurrentDateService getCurrentDateService() {
        return currentDateService;
    }
//
//	/**
//	 * Constructs a SQL where statement, by only including not null values, and
//	 * correctly inserting WHERE/AND/OR keywords
//	 * 
//	 * Eg  formatter.sqlWhere("AND",true,startDate,endDate)
//	 * might return any of
//	 * - nothing, if dates are null
//	 * - WHERE start
//	 * 
//	 * 
//	 * @param args
//	 * @return 
//	 */
//    public SqlBuilder getSqlBuilder() {
//		return new SqlBuilder();
//	}
//	
//	public static class SqlBuilder {
//		
//		private StringBuilder select = new StringBuilder();
//		
//		private StringBuilder from = new StringBuilder();
//		
//		private StringBuilder where;
//		
//		private String 
//		
//		public SqlBuilder select(String selectFields) {
//			select.append(select);
//			return this;
//		}
//		
//		public SqlBuilder from(String fromTables) {
//			from.append(fromTables);
//			return this;
//		}		
//		
//		public SqlBuilder where(String field, String value) {
//			if( value == null ) {
//				return this;
//			}
//			if( where == null) {
//				where = new StringBuilder("WHERE");
//			}
//			return this;
//		}
//		
//		public SqlBuilder whereQ(String field, String value) {
//			
//		}		
//		
//	}
}
