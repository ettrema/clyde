
package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.console.Result;
import java.util.List;
import java.util.Map.Entry;

public class ThreadDump extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThreadDump.class);
    
    ThreadDump(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    public Result execute() {
        StringBuilder sb = new  StringBuilder();
        sb.append("<ol>");
        for( Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread th = entry.getKey();
            sb.append("<li>");
            sb.append(th.getName()).append(" - ").append(th.getId()).append(" - ").append(th.getState());
            sb.append("<ul>");
            for( StackTraceElement item : entry.getValue()) {
                sb.append("<li>");
                sb.append(item.getClassName()).append(".").append(item.getMethodName()).append("(").append(item.getLineNumber()).append(")");
                sb.append("</li>");
            }
            sb.append("</ul>");
            
            sb.append("</li>");
        }
        sb.append("</ol>");
        return result(sb.toString());
    }

}
