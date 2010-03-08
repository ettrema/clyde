
package com.bradmcevoy.web.console2;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.ettrema.console.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Du extends AbstractConsoleCommand{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Du.class);
    
    public Du(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    
    
    @Override
    public Result execute() {
        Folder cur = currentResource();
        if( cur == null ) {
            return result("current dir not found: " + currentDir);
        }        
        if( cur instanceof Folder ) {
            Folder col = (Folder) cur;
            StringBuffer sb = new StringBuffer();
            sb.append("<table border='0' cellpadding='2' cellspacing='2'>").append("\n");
            for( Entry e : getSizes(col) ) {
                sb.append("<tr>");
                String color = e.getColor();
                sb.append("<td>")
                        .append("<a href='").append(e.href).append("'>").append("<font color='").append(color).append("'>").append(e.name).append("</font></a>")
                        .append("</td>");
                sb.append("<td><font color='").append(color).append("'>").append(e).append("</font></td>");
                sb.append("</tr>").append("\n");
            }
            sb.append("</table>");
            return result(sb.toString());
        } else {
            return result("not a collection: " + cur.getHref());
        }
        
    }

    List<Entry> getSizes(Folder col) {
        List<Entry> list = new ArrayList<Entry>();                
        for( Resource r : col.getChildren() ) {
            Long size;
            if( r instanceof Folder ) {
                Folder f = (Folder) r;
                size = f.getTotalSize();
            } else if( r instanceof GetableResource) {
                GetableResource gr = (GetableResource)r;
                size = gr.getContentLength();
            } else {
                size = null;
            }

            if( r instanceof BaseResource ) {
                if( size == null ) size = 0l;
                BaseResource ct = (BaseResource) r;
                long dataSize = ct.getPersistedSize();
                size += dataSize;
                list.add(new Entry(size,ct.getHref(),r.getName()));
            }            
        }
        Collections.sort(list);
        return list;
    }
    
    private class Entry implements Comparable<Entry> {
        final Long size;
        final String href;
        final String name;

        public Entry(Long size, String href, String name) {
            this.size = size;
            this.href = href;
            this.name = name;
        }

        private String getColor() {
            if( size == null ) return "black";
            if( size >= 1000000000) return "red";
            if( size >= 1000000) return "orange";
            if( size >= 1000) return "green";
            return "black";
        }
        
        @Override
        public String toString() {
            if( size == null ) return "N/A";
            if( size >= 1000000000) return size/1000000000 + "Gb";
            if( size >= 1000000) return size/1000000 + "Mb";
            if( size >= 1000) return size/1000 + "Kb";
            return size + "b";
        }

        
        
        @Override
        public int compareTo(Entry o) {
            if( size == null ) {
                if( o.size == null ) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                if( o.size == null ) {
                    return -1;
                } else {
                    return size.compareTo(o.size) * -1;
                }
            }
        }
        
        
    }
}
