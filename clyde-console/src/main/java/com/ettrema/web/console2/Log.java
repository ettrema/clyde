
package com.ettrema.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class Log extends AbstractConsoleCommand{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Log.class);
    
    public static final int MAX_LENGTH = 100;
    
    Log(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    @Override
    public Result execute() {
        System.out.println("log");
        int maxLength = MAX_LENGTH*100;
        if( args.size() > 0 ) {
            String sLength = args.get(0);
            if( sLength != null && sLength.length()>0 ) {
                maxLength = Integer.parseInt(sLength)*100; // approx line length
            }
        }
        System.out.println("log length: " + maxLength);
        FileReader reader = null;
        try {
            String sFile = ctx().get("log.file");
            File file = new File(sFile);
            if (!file.exists()) {
                return result("log file doesnt exist: " + sFile);
            }
            long length = file.length();
            reader = new FileReader(file);
            long start = (length > maxLength) ? length-maxLength : 0;
            long skipped = reader.skip(start);
            StringBuffer sb = new StringBuffer();
                        
            int s = reader.read();
            while( s > -1 ) {
                char ch = (char) s;
                if( ch == '\n') {
                    sb.append("<br/>");
                    sb.append('\n');                    
                } else {
                    sb.append(ch);
                }
                s = reader.read();
            }
            return result(sb.toString());
        } catch (FileNotFoundException ex) {
            log.error("err", ex);
            return result("File not found: " + ex.getMessage());
        } catch (IOException ex) {
            log.error("err", ex);
            return result("IOException: " + ex.getMessage());
        } finally {
            IOUtils.closeQuietly( reader );
        }
    }


}
