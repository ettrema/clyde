
package com.ettrema.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.Result;
import static com.ettrema.context.RequestContext.*;
import com.ettrema.grid.AsynchProcessor;
import java.util.ArrayList;
import java.util.List;

public class Patch extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Patch.class);

    Patch(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }
    
    @Override
    public Result execute() {
        if( args.isEmpty() ) return result("Please enter a class name");
        String sClazz = args.get(0);
        if( sClazz == null ) return result("Please enter a class name");
        Object oPatch = initInstance(sClazz);
        if( oPatch == null ) return result("invalid patch class name");
        if( oPatch instanceof PatchApplicator ) {
            PatchApplicator p = (PatchApplicator) oPatch;
            String[] patchArgs = getPatchArgs();
            log.debug("patch args: " + patchArgs);
            p.setArgs(patchArgs);
            p.setCurrentFolder( currentResource() );
            _(AsynchProcessor.class).enqueue(p);

            return new Result(this.currentDir, "executed (or executing) patch " + p);
        } else {
            return result("class is not a PatchApplicator");
        }
        
    }

    private String[] getPatchArgs() {
        List<String> list = new ArrayList<String>(this.args);
        list.remove(0);
        String[] arr = new String[list.size()];
        list.toArray(arr);
        return arr;
    }

    private Object initInstance(String sClazz) {
        try {
            Class c = Class.forName(sClazz);
            return c.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(sClazz,ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(sClazz,ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(sClazz,ex);
        }
    }
}
