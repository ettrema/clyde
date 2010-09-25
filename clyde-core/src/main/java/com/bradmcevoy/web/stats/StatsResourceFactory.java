package com.bradmcevoy.web.stats;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.web.*;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.mvel.TemplateInterpreter;

import static com.ettrema.context.RequestContext._;

public class StatsResourceFactory extends CommonResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StatsResourceFactory.class);

    ResourceFactory authResourceFactory;
    
    public StatsResourceFactory() {
    }

    public StatsResourceFactory(ResourceFactory authResourceFactory) {
        this.authResourceFactory = authResourceFactory;
    }

    public void init(HttpManager manager) {
        this.authResourceFactory = manager.getResourceFactory();
    }
    
    @Override
    public Resource getResource(String host, String url) {
        if( authResourceFactory == null ) throw new NullPointerException("authResourceFactory is null");
//        log.debug("getResource: host=" + host + " url=" + url);        
        Path path = Path.path(url);

        Resource r = getResource(host, path);
        return r;
    }

    private BaseStatsResource getResource(String host, Path path) {
        if( path == null ) return null;
        if( path.getName() != null && path.getName().equals("stats")) {
            if (path.getParent() != null && path.getParent().isRoot()) {
                return new StatsHost(host);
            } else {
                return null;
            }
        } else {
            BaseStatsResource parent = getResource(host, path.getParent());
            if( parent == null ) return null;
            if( parent instanceof BaseStatsFolderResource ) {
                BaseStatsFolderResource folder = (BaseStatsFolderResource)parent;
                return folder.lookupChild(path.getName());
            } else {
                return null;
            }                        
        }
    }

    public abstract class BaseStatsResource implements Resource {

        abstract Host host();

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            _(PermissionChecker.class).hasRole( Role.ADMINISTRATOR, host(), auth);
            return (auth!=null);
        }

        @Override
        public Date getModifiedDate() {
            return new Date();
        }

        public Long getContentLength() {
            return null;
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        public String getContentType(String accepts) {
            return "application/x-javascript; charset=utf-8";
        }
        
    }
    
    private String suffixSlash(String s) {
            if (!s.endsWith("/")) {
                s = s + "/";
            }
            return s;
    }
    
    public abstract class BaseStatsFolderResource extends BaseStatsResource implements GetableResource {
        
        abstract BaseStatsResource lookupChild(String name);

        public abstract BaseStatsFolderResource getParent();
        
        @Override
        public String checkRedirect(Request request) {
            log.debug("checkRedirect");
            return suffixSlash(request.getAbsoluteUrl()) + "index.html";
        }
        
        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
            // nothing to do
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return 60l;
        }        
    }

    public class Tuple {
        public String key;
        public int value;
        
        Tuple(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    
    public class TupleList extends ArrayList<Tuple> {
        private static final long serialVersionUID = 1L;
        
    }
    
    public class StatsHost extends BaseStatsFolderResource {

        String host;

        List<StatsYear> years;
        
        public StatsHost(String host) {
            this.host = host;
        }

        @Override
        public Object authenticate(String user, String password) {
            if( authResourceFactory == null ) throw new NullPointerException("resource fatory is null");
            Host h = host();
            return h.authenticate(user, password);
        }

        public Host host() {
            Resource r = authResourceFactory.getResource(host, "/");
            if( r == null ) throw new RuntimeException("Couldnt locate host: " + host);
            if( r instanceof Host ) {
                Host h = (Host)r;
                return h;
            } else {
                throw new RuntimeException("Couldnt locate host: " + host);
            }
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return (auth != null);
        }

        @Override
        public String getRealm() {
            return host().getRealm();
        }
                        
        @Override
        StatsResourceFactory.BaseStatsResource lookupChild( String year) {
            if( "index.html".equals(year) ) {
                return new StatsHostIndex(this);
            }
            int y;
            try {
                y = Integer.parseInt(year);
            } catch ( NumberFormatException e) {
                return null;
            }
            return new StatsYear(this, y);
        }

        public String getHref() {
            return "/" + getName() + "/";
        }

        @Override
        public String getName() {
            return "stats";
        }

        @Override
        public StatsResourceFactory.BaseStatsFolderResource getParent() {
            return null;
        }
        
        public String getHostName() {
            return host;
        }
        
        public List<StatsYear> getYears() {
            if (years != null) {
                return years;
            }
            try {
                years = new ArrayList<StatsResourceFactory.StatsYear>();
                String sql = "select log_year from accesslog where " + getCriteria() + " group by log_year order by log_year";
                Connection con = requestContext().get(Connection.class);
                CallableStatement stmt = con.prepareCall(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int i = rs.getInt(1);
                    StatsYear y = new StatsYear(this, i);
                    years.add(y);
                }
                rs.close();
                return years;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            
        }

        private String getCriteria() {
            return "log_host = '" + host + "'";
        }

        @Override
        public String getUniqueId() {
            return null;
        }
    }
    
    public class StatsHostIndex extends BaseIndex {        
        public StatsHostIndex(StatsHost host) {
            super(host,"host.index.mvel");
        }
    }

    public class StatsYear extends BaseStatsFolderResource {

        StatsHost host;
        int year;
        private List<SummaryMonth> summaryMonths;

        public StatsYear(StatsHost host, int year) {
            this.host = host;
            this.year = year;
        }

        @Override
        public Host host() {
            return host.host();
        }

        public String getRealm() {
            return host.getRealm();
        }

        @Override
        public String getUniqueId() {
            return null;
        }
        @Override
        public Object authenticate(String user, String password) {
            return host.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return host.authorise(request, method, auth);
        }                
        
        @Override
        StatsResourceFactory.BaseStatsResource lookupChild( String monthName) {
            if( monthName.equals("index.html"))  {
                return new StatsYearIndex(this);
            } else {
                return new StatsMonth(this, monthName);
            }
        }

        public String getHref() {
            return host.getHref() + getName() + "/";
        }

        @Override
        public String getName() {
            return year + "";
        }

        @Override
        public StatsResourceFactory.BaseStatsFolderResource getParent() {
            return host;
        }

        private String getCriteria() {
            return host.getCriteria() + " AND log_year = " + year;
        }

        public List<SummaryMonth> getSummaryMonths() {
            if (summaryMonths != null) return summaryMonths;
            try {
                summaryMonths = new ArrayList<SummaryMonth>();
                Connection con = requestContext().get(Connection.class);
                String sql = getSummaryMonthsSql();
                CallableStatement stmt = con.prepareCall(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    summaryMonths.add(new SummaryMonth(rs));
                }
                rs.close();
                return summaryMonths;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        private String getSummaryMonthsSql() {
            return "select log_month,count(*), sum(log_size) from accesslog where " + getCriteria() + " group by log_month order by log_month";
        }
        
    }

    public class SummaryMonth {
        public final String month;
        public final int hits;
        public final int kbytes;

        public SummaryMonth(ResultSet rs) throws SQLException {
            this.month = rs.getString(1);
            this.hits = rs.getInt(2);
            this.kbytes = rs.getInt(3)/1000;
        }        
    }
    
    public class BaseIndex<T extends BaseStatsFolderResource> extends BaseStatsResource implements GetableResource {

        T parent;
        String mvelTemplate;
        
        public BaseIndex(T parent, String mvelTemplate) {
            this.parent = parent;
            this.mvelTemplate = mvelTemplate;
        }

        @Override
        Host host() {
            return parent.host();
        }

        public String getRealm() {
            return parent.getRealm();
        }

        @Override
        public String getUniqueId() {
            return null;
        }
        @Override
        public Object authenticate(String user, String password) {
            return parent.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return parent.authorise(request, method, auth);
        }                
        

        @Override
        public String getName() {
            return "index.html";
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
            String template = FileUtils.readResource(this.getClass(), mvelTemplate);
            Map map = new HashMap();
            String s = TemplateInterpreter.evalToString(template, this, map);
            out.write(s.getBytes());
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return 60l;
        }
        
        public T getParent() {
            return parent;
        }
        
    }
    
    public class StatsYearIndex extends BaseIndex<StatsYear> {
        public StatsYearIndex(StatsYear year) {
            super(year,"year.index.mvel");
        }        
    }
    
    public class StatsMonth extends BaseStatsFolderResource {

        StatsYear year;
        String monthDescriptor;
        int monthNum;
        String monthName;

        public StatsMonth(StatsYear year, String monthName) {
            this.year = year;
            this.monthDescriptor = monthName;
            
            int pos = monthDescriptor.indexOf("_");
            if( pos > 0 ) {
                String s = monthDescriptor.substring(0,pos-1);
                this.monthNum = Integer.parseInt(s)-1;
                this.monthName = monthDescriptor.substring(pos+1);
            } else {
                throw new RuntimeException("Invalid month name format: " + monthDescriptor);
            }            
        }

        @Override
        Host host() {
            return year.host();
        }



        public String getRealm() {
            return year.getRealm();
        }
        
        @Override
        public String getUniqueId() {
            return null;
        }
        @Override
        public Object authenticate(String user, String password) {
            return year.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return year.authorise(request, method, auth);
        }                
        
        @Override
        StatsResourceFactory.BaseStatsResource lookupChild( String name) {
            if (name.equals("day.png")) {
                return new DayChart(this);
            } else if( name.equals("index.html")) {
                return new MonthReport(this);
            }
            return null;
        }

        public String getHref() {
            return year.getHref() + getName() + "/";
        }

        @Override
        public String getName() {
            return monthDescriptor;
        }

        public String getMonthName() {
            return monthName;
        }

        public int getMonthNum() {
            return monthNum;
        }                
        
        private int getNumDays() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year.year);
            cal.set(Calendar.MONTH, monthNum);
            int numDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            return numDays;
        }

        @Override
        public StatsResourceFactory.BaseStatsFolderResource getParent() {
            return year;
        }

        public String getCriteria() {
            return year.getCriteria() + " AND log_month = '" + monthDescriptor + "'";
        }
        
    }
    
    public class DayChart extends BaseStatsResource implements GetableResource {

        StatsMonth parent;
        
        public DayChart(StatsMonth parent) {
            this.parent = parent;
        }

        @Override
        Host host() {
            return parent.host();
        }



        public Object authenticate( String user, String password ) {
            return parent.authenticate( user, password );
        }

        public String getRealm() {
            return parent.getRealm();
        }
        
        @Override
        public String getUniqueId() {
            return null;
        }
        @Override
        public String getContentType(String accepts) {
            return "image/png";
        }

        public String getHref() {
            return parent.getHref() + getName();
        }
       
        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return 60l;
        }        
        
        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
            // Create a simple Bar chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            try{
                fill(dataset);
            } catch(SQLException e) {
                throw new RuntimeException(e);
            }
            JFreeChart chart = ChartFactory.createBarChart("Hits by Day",
                    "Day", "Hits", dataset, PlotOrientation.VERTICAL, false,
                    true, false);
            try {
                ChartUtilities.writeChartAsPNG(out, chart, 600, 400);
                out.flush();
            } catch (IOException e) {
                System.err.println("Problem occurred creating chart.");
            }
        }        
        
        @Override
        public String getName() {
            return "day.png";
        }

        void fill(DefaultCategoryDataset dataSet) throws SQLException {
            String sql = getDaySql();
            Connection con = requestContext().get(Connection.class);
            CallableStatement stmt = con.prepareCall(sql);
            ResultSet rs = stmt.executeQuery();
            TupleList list = initialTuples();
            while( rs.next() ) {
                int day = rs.getInt(1);
                int count = rs.getInt(2);
                Tuple t = list.get(day-1);
                t.value = count;
            }
            rs.close();
            for( Tuple t : list ) {
                dataSet.setValue(t.value, "Number of Hits",t.key);
            }
        }       
        
        TupleList initialTuples() {
            TupleList list = new TupleList();
            int numDays = parent.getNumDays();
            for (int i = 1; i <= numDays; i++) {
                Tuple t = new Tuple(i+"",0);
                list.add(t);
            }
            return list;
        }
        
        String getDaySql() {
            return "select log_day,COUNT(*) from accesslog where " + parent.getCriteria() + " group by log_day order by log_day";
        }
    }
    
    public class MonthReport extends BaseIndex<StatsMonth> {
        private List<Totals> totals;
        private List<UrlStats> topUrls;
        
        public MonthReport(StatsMonth month) { 
            super(month,"month.index.mvel");
        }
        
        public List<Totals> getTotals() {
            if (totals != null) return totals;
            try {
                totals = new ArrayList<StatsResourceFactory.Totals>();
                Connection con = requestContext().get(Connection.class);
                String sql = getTotalsSql();
                CallableStatement stmt = con.prepareCall(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    totals.add(new Totals(rs));
                }
                rs.close();
                return totals;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        String getTotalsSql() {
            return "select log_content_type, count(*), sum(log_size) from accesslog where " + parent.getCriteria() + " group by log_content_type order by count(*) DESC";
        }

        public List<UrlStats> getTopUrls() {
            if (topUrls != null) return topUrls;
            try {
                topUrls = new ArrayList<UrlStats>();
                Connection con = requestContext().get(Connection.class);
                String sql = getTopUrlsSql();
                CallableStatement stmt = con.prepareCall(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    topUrls.add(new UrlStats(rs));
                }
                rs.close();
                return topUrls;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        private String getTopUrlsSql() {
            return "select log_url,count(*) from accesslog where log_content_type = 'text/html' and " + parent.getCriteria() + " group by log_url order by count(*) desc";
        }
        
    }
    
    public class Totals {
        public String contentType;
        public int hits;
        public long size;
        
        public Totals(ResultSet rs) {
            try {
                contentType = rs.getString(1);
                hits = rs.getInt(2);
                size = rs.getLong(3) / 1000;                
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        public String getContentType() {
            return contentType;
        }

        public int getHits() {
            return hits;
        }

        public long getSize() {
            return size;
        }                
    }
    
    public class UrlStats {
        public String url;
        public int hits;

        public UrlStats(ResultSet rs) {
            try {
                this.url = rs.getString(1);
                this.hits = rs.getInt(2);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        public int getHits() {
            return hits;
        }

        public String getUrl() {
            return url;
        }               
    }

}
