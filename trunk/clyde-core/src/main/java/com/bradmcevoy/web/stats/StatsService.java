package com.bradmcevoy.web.stats;

import com.bradmcevoy.utils.DateUtils;
import com.bradmcevoy.web.Host;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class StatsService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( StatsService.class );
    private String tableName = "accesslog";

    public int activeHosts( String baseDomain, String method, int numDays ) {
        log.trace( "activeHosts" );
        Date to = new Date();
        Date from = DateUtils.addDays( to, numDays * -1 );

        ResultSet rs = null;
        CallableStatement stmt = null;
        String sql = "";
        try {
            List<Object> params = new ArrayList<Object>();
            String select = "SELECT count(*) FROM " + tableName;
            String where = whereAndParam( null, "log_date", ">", from, params );
            where = whereAndParam( where, "log_date", "<", to, params );
            where = whereAndParam( where, "log_host", "LIKE", "%" + baseDomain, params );
            where = whereAndParam( where, "log_method", "=", method, params );
            sql = select + " WHERE " + where + " GROUP BY log_host";
            if( log.isTraceEnabled() ) {
                log.trace( sql );
            }
            stmt = _( Connection.class ).prepareCall( sql );
            for( int i = 0; i < params.size(); i++ ) {
                set( stmt, i, params.get( i ) );
            }
            rs = stmt.executeQuery();
            if( rs.next() ) {
                return rs.getInt( 1 );
            } else {
                return 0;
            }
        } catch( SQLException ex ) {
            throw new RuntimeException( sql, ex );
        } finally {
            close( rs );
            close( stmt );
        }
    }

    public int queryLastDays( Host host, String path, int numDays ) {
        return queryLastDays( host, path, numDays, null );
    }

    public int queryLastDays( Host host, String path, int numDays, String method ) {
        Date to = new Date();
        Date from = DateUtils.addDays( to, numDays * -1 );
        return query( from, to, host.getName(), path, method );
    }

    public int query( Date from, Date to, String host, String path, String method ) {
        log.trace( "query" );
        ResultSet rs = null;
        CallableStatement stmt = null;
        String sql = "";
        try {
            List<Object> params = new ArrayList<Object>();
            String select = "SELECT count(*) FROM " + tableName;
            String where = whereAndParam( null, "log_date", ">", from, params );
            where = whereAndParam( where, "log_date", "<", to, params );
            where = whereAndParam( where, "log_host", "=", host, params );
            where = whereAndParam( where, "log_url", "=", path, params );
            where = whereAndParam( where, "log_method", "=", method, params );
            sql = select + " WHERE " + where;
            stmt = _( Connection.class ).prepareCall( sql );
            for( int i = 0; i < params.size(); i++ ) {
                set( stmt, i, params.get( i ) );
            }
            rs = stmt.executeQuery();
            if( rs.next() ) {
                return rs.getInt( 1 );
            } else {
                return 0;
            }
        } catch( SQLException ex ) {
            throw new RuntimeException( sql, ex );
        } finally {
            close( rs );
            close( stmt );
        }
    }

    private void set( CallableStatement stmt, int pos, Object val ) throws SQLException {
        if( val instanceof String ) {
            stmt.setString( pos + 1, (String) val );
        } else if( val instanceof Date ) {
            Date dt = (Date) val;
            Timestamp ts = new Timestamp( dt.getTime() );
            stmt.setTimestamp( pos + 1, ts );
        } else {
            throw new RuntimeException( "unsupported value type: " + val.getClass() );
        }
    }

    private String whereAndParam( String where, String name, String operator, Object value, List<Object> params ) {
        if( value == null ) return where;
        params.add( value );
        return whereAnd( where, name + " " + operator + " ?" );
    }

    private String whereAnd( String where, String exp ) {
        if( exp == null ) return where;
        if( where != null ) {
            where += " AND ";
        } else {
            where = "";
        }
        where += exp;
        return where;
    }

    private void close( ResultSet rs ) {
        if( rs == null ) return;
        try {
            rs.close();
        } catch( SQLException ex ) {
        }
    }

    private void close( CallableStatement rs ) {
        if( rs == null ) return;
        try {
            rs.close();
        } catch( SQLException ex ) {
        }
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName( String tableName ) {
        this.tableName = tableName;
    }
}
