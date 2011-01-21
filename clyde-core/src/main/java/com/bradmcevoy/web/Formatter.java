package com.bradmcevoy.web;

import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.DateVal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTime;

/**
 * Handy functions exposes to rendering logic for formatting.
 *
 * @author brad
 */
public class Formatter {

    private static final Formatter theInstance = new Formatter();
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
        return theInstance;
    }

    public BigDecimal toDecimal(Object o, int places) {
        if (o == null) {
            return BigDecimal.ZERO;
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

    public Long toLong(Object oLimit, boolean withNulls) {
        Long limit;
        if (oLimit == null) {
            limit = withNulls ? null : 0l;
        } else if (oLimit instanceof Long) {
            limit = (Long) oLimit;
        } else if (oLimit instanceof Integer) {
            int i = (Integer) oLimit;
            limit = (long) i;
        } else if (oLimit instanceof String) {
            String s = (String) oLimit;
            if (s.length() == 0) {
                limit = withNulls ? null : 0l;
            } else {
                limit = Long.parseLong(s);
            }
        } else if (oLimit instanceof ComponentValue) {
            ComponentValue cv = (ComponentValue) oLimit;
            limit = toLong(cv.getValue());
        } else {
            throw new RuntimeException("unsupported class: " + oLimit.getClass());
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

    public org.joda.time.DateTime getDateTime(Object o) {
        if (o == null) {
            return null;
        }
        return new DateTime(o);
    }

    /**
     * Format as a percentage, including a percentage symbol and where blank/null
     * values result in a blank output
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
     * @param appendSymbol - if true the percentage symbol is appended if a non-blank value
     * @param withBlanks - if true, blank numerators or divisors result in a blank value. Otherwise return zero.
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
            return o.toString();
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
}
