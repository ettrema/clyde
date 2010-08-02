package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.DateUtils;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Templatable;
import com.ettrema.console.ResultFormatter;
import java.util.Date;
import java.util.List;

/**
 *
 * @author brad
 */
public class ClydeLsResultFormatter implements ResultFormatter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydeLsResultFormatter.class );

    private DateUtils du = new DateUtils();

    public String format( String href, Resource r1 ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "<tr>" );
        if( r1 instanceof Folder ) {
            Folder f = (Folder) r1;
            String url = f.getUrl();
            addCell( sb, "<a href=\"javascript:runCmd('cd " + url + "')\">[CD]</a><a href=\"javascript:runCmd('ls " + url + "')\">[LS]</a>" );
        } else {
            addCell( sb, null );
        }
        addCell( sb, "<a target='_new' href='" + href + ".source.edit'>[src]</a>" );
        addCell( sb, "<a target='_new' href='" + href + "'>" + r1.getName() + "</a>" );
        if( r1 instanceof Templatable ) {
            Templatable t = (Templatable) r1;
            addCell( sb, t.getTemplateName() );
        } else {
            addCell( sb, null );
        }
        addCell( sb, r1.getRealm() );
        Date mod = r1.getModifiedDate();
        if( mod == null ) {
            log.warn( "null mod date for: " + r1.getClass().getCanonicalName());
            addCell( sb, "" );
        } else {
            addCell( sb, du.getText( mod ) );
        }
        if( r1 instanceof BaseResource ) {
            BaseResource b = (BaseResource) r1;
            addCell( sb, b.getNameNodeId() );
        } else {
            addCell( sb, null );
        }
        addCell( sb, r1.getClass().getCanonicalName() );
        if( r1 instanceof BaseResource ) {
            BaseResource b = (BaseResource) r1;
            addCell( sb, b.getContentLength() );
        } else {
            addCell( sb, null );
        }
        sb.append( "</tr>" );
        return sb.toString();
    }

    public String begin( List<? extends Resource> list ) {
        String s = "<table cellpadding='3'>";
        s += "<tr>";
        s += "<th></th><th></th><th>Name</th><th>Template</th><th>Realm</th><th>Modified</th><th>NameNodeID</th><th>Class</th><th>Size</th>";
        s += "</tr>";
        return s;
    }

    public String end() {
        return "<table>";
    }

    private void addCell( StringBuffer sb, Object o ) {
        sb.append( "<td>" );
        if( o != null ) {
            sb.append( o.toString() );
        }
        sb.append( "</td>" );
    }
}
