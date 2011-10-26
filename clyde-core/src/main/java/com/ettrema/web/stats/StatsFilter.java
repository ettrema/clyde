package com.ettrema.web.stats;

import com.bradmcevoy.http.AbstractResponse;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.Filter;
import com.bradmcevoy.http.FilterChain;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;
import com.ettrema.vfs.VfsCommon;
import com.ettrema.context.Context;
import com.ettrema.context.Executable2;
import com.ettrema.context.RootContext;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class StatsFilter extends VfsCommon implements Filter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StatsFilter.class);
    final private RootContext rootContext;
    private final java.util.concurrent.LinkedBlockingQueue<AccessLog> queue = new LinkedBlockingQueue<AccessLog>();
    private Thread threadInserter;

    public StatsFilter(RootContext rootContext) {
        if (rootContext == null) {
            throw new NullPointerException("No rootContext");
        }
        
        this.rootContext = rootContext;
        log.warn( "Hello from the StatsFilter");
    }

    
    
    @Override
    public void process(FilterChain chain, Request request, Response response) {
        long t = System.currentTimeMillis();
        CountingResponse countingResponse = new CountingResponse(response);
        try {
            //chain.process(request, response);
            chain.process(request, countingResponse);
        } finally {
            countingResponse.flush();
            t = System.currentTimeMillis() - t;
            log(request, response, t, countingResponse.size(), request.getMethod().code);
        }
    }
    
    public class CountingResponse extends AbstractResponse implements Response {

        final Response wrapped;
        //final OutputStream out;
        final CountingOutputStream out;
        
        public CountingResponse(Response wrapped) {
            this.wrapped = wrapped;
            out = new CountingOutputStream(wrapped.getOutputStream());
            //out = wrapped.getOutputStream();
        }

        void flush() {
            try {
                out.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        int size() {
            return out.count;
            //return 0;
        }
        
        @Override
        public String getNonStandardHeader(String code) {
            return wrapped.getNonStandardHeader(code);
        }                

        @Override
        public Status getStatus() {
            return wrapped.getStatus();
        }

        @Override
        public void setStatus(Status status) {
            wrapped.setStatus(status);
        }

        @Override
        public void setNonStandardHeader(String string, String string0) {
            wrapped.setNonStandardHeader(string, string0);
        }

        @Override
        public OutputStream getOutputStream() {
            return out;
        }

        @Override
        public void close() {
            wrapped.close();
        }

        @Override
        public Map<String, String> getHeaders() {
            return wrapped.getHeaders();
        }

        @Override
        public void setAuthenticateHeader( List<String> challenges ) {
            wrapped.setAuthenticateHeader( challenges );
        }

        public Cookie setCookie( Cookie cookie ) {
            return wrapped.setCookie( cookie );
        }

        public Cookie setCookie( String name, String value ) {
            return wrapped.setCookie( name, value );
        }
    }

    private void log(Request request, Response response, long duration, int size, String method) {
        String host = request.getHostHeader();
        if( host == null ) host = "";
        String h = host.replace("test.", "www."); // TODO: nasty little hack. need to bring statsfilter together with statsresourcefactory
        String path = request.getAbsolutePath();
        String referrerUrl = request.getRefererHeader();
        int result = response.getStatus() == null ? 500 : response.getStatus().code;
        String from = request.getFromAddress();
        AccessLog a = new AccessLog(h, path, referrerUrl, result, duration, size, method, response.getContentTypeHeader(),from);
        queue.add(a);

    }
    
    public void init() {
        log.trace("starting stats logging daemon");
        threadInserter = new Thread(new Inserter());
        threadInserter.setDaemon(true);
        threadInserter.start();

    }

    private class Inserter implements Runnable {

        @Override
        public void run() {
            boolean running = true;
            while (running) {
                try {
                    AccessLog a = queue.take();
                    log.trace("insert log");
                    doInsert(a);
                } catch (InterruptedException ex) {
                    log.warn("inserter operation terminated", ex);
                    running = false;
                }
            }
            log.warn("inserter stopped");
        }

    private final static String SQL_INSERT = "INSERT INTO accesslog(" +
            "log_host,log_url,log_referrer_url,log_date,log_year,log_month,log_day,log_hour,log_result_code,log_duration,log_size,log_method,log_content_type,log_from)" +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        private void doInsert(final AccessLog accessLog) {
            rootContext.execute(new Executable2() {

                @Override
                public void execute(Context context) {
                    Connection con = context.get(Connection.class);
                    try {                        
                        PreparedStatement stmt = con.prepareStatement(SQL_INSERT);
                        stmt.setString(1, accessLog.host);
                        stmt.setString(2, accessLog.url);
                        stmt.setString(3, accessLog.referrerUrl);
                        Date dt = new Date();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dt);
                        stmt.setDate(4, new java.sql.Date(dt.getTime()));
                        stmt.setInt(5, cal.get(Calendar.YEAR));
                        stmt.setString(6, monthName(cal));
                        stmt.setInt(7, cal.get(Calendar.DAY_OF_MONTH));
                        stmt.setInt(8, cal.get(Calendar.HOUR_OF_DAY));
                        stmt.setInt(9, accessLog.result);
                        stmt.setLong(10, accessLog.duration);
                        stmt.setInt(11, accessLog.size);
                        stmt.setString(12, accessLog.method);
                        stmt.setString(13,accessLog.contentType);
                        stmt.setString(14, accessLog.fromAddress);
                        stmt.execute();
                    } catch (SQLException ex) {
                        log.error("Exception logging access",ex);
                        rollback(con);
                    } finally {
                        commit(con);
                    }
                }
            });
        }
    }
    
    public static String monthName(Calendar cal) {
        int monthNum = cal.get(Calendar.MONTH) + 1;
        String s = "";
        if( monthNum < 10 ) s = "0";
        s = s + monthNum;
        s = s + "_" + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
        return s;
    }

    private void commit(Connection con) {        
        try {
            con.commit();
        } catch (SQLException ex) {
            log.error("Exception commiting log", ex);
        }
    }
    
    private void rollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException ex) {
            log.error("Exception rolling back log", ex);
        }
    }
    
    public class AccessLog {

        private String host;
        private String url;
        private String referrerUrl;
        private int result;
        private long duration;
        private int size;
        private String method;
        private String contentType;
        private String fromAddress;

        public AccessLog(String host, String url, String referrerUrl, int result, long duration, int size, String method, String contentType, String fromAddress) {
            this.host = host;
            this.url = url;
            this.referrerUrl = referrerUrl;
            this.result = result;
            this.duration = duration;
            this.size = size;
            this.method = method;
            this.contentType = contentType;
            this.fromAddress = fromAddress;
        }
    }
}
