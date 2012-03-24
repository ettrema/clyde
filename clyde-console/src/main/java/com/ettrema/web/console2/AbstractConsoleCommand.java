
package com.ettrema.web.console2;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.web.BaseResource;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.Templatable;
import com.ettrema.console.ConsoleCommand;
import com.ettrema.console.Result;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.VfsSession;
import com.ettrema.vfs.VfsTransactionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractConsoleCommand implements ConsoleCommand{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractConsoleCommand.class);
    
    protected final List<String> args;
    /**
     * The current host name
     */
    protected final String host;
    protected String currentDir;
    protected final ResourceFactory resourceFactory;
    
    AbstractConsoleCommand(List<String> args, String host, String currentDir,ResourceFactory resourceFactory) {
        this.args = args;
        this.host = host;
        this.currentDir = currentDir;
        this.resourceFactory = resourceFactory;
    }    
    
    protected Context ctx() {
        return RequestContext.getCurrent();
    }
    
    protected void commit() {        
        VfsTransactionManager.commit();
    }
    
    protected Resource find(Path p) {
        return resourceFactory.getResource(host, p.toString());
    }
    
    protected Folder currentResource() {
        return (Folder) resourceFactory.getResource(host, currentDir);
    }
    
    protected Result result(String msg) {
        return new Result(currentDir,msg);
    }    
    
    protected BaseResource find(BaseResource cur, Path path) {
        log.debug("find: " + cur.getHref() + " - " + path);
        if( path.isRoot() && !path.isRelative() ) {
            return host();
        }
        if( cur == null ) return null;
        Path parent = path.getParent();
        String moveTo = path.getName();
        
        if( parent == null ) {
            return move(cur,moveTo);
        } else {
            BaseResource r = find(cur,parent);
            return move(r,moveTo);
        }
    }

    protected BaseResource move(BaseResource cur, String p) {
        log.debug("move: " + cur.getClass());
        if( p.equals("..")) {
            if( cur instanceof Templatable ) {
                Templatable t = (CommonTemplated)cur;
                t = t.getParent();
                return  (BaseResource) t;
            } else {
                throw new RuntimeException("Cant go up from type: " + cur.getClass() + " - " + cur.getName());
            }
        } else if( p.equals(".") ) {
            return cur;
        } else {
            log.debug("..find child");
            if( cur instanceof CollectionResource ) {
                CollectionResource col = (CollectionResource)cur;
                return (BaseResource) col.child(p);
            } else {
                throw new RuntimeException("Not a folder: " + cur.getName());
            }
        }
    }
    
    protected Host host() {
        return (Host) find(Path.root);
    }    
    
    public static String pathFromHref(String href) {
        log.debug("pathFromHref: " + href);
        int pos = href.indexOf("//");
        if( pos < 5 ) {
            log.debug("double slash not found (//) not found. pos: " + pos + " in: " + href);
            return null;
        }
        String s = href.substring(pos+2, href.length());
        pos = s.indexOf("/");
        s = s.substring(pos,s.length());
        return s;
    }
    
    /**
     * Note that only the last part of the path is matched against a regular expression
     * 
     * @param cur
     * @param path
     * @param list
     * @return
     */
    protected Result findWithRegex(Folder cur, Path path, List<BaseResource> list) {
        if(path.getLength() > 1) {
            String first = path.getFirst();
            if( "**".equals( first ) ) {
                List<Folder> folders = crawl(cur);
                for( Folder f : folders ) {
                    findInFolderWithRegex( f, path, list );
                }
                return null;
            } else {
                return findInFolderWithRegex( cur, path, list );
            }
        } else {
            return findInFolderWithRegex( cur, path, list );
        }
    }

    protected Result findInFolderWithRegex(Folder cur, Path path, List<BaseResource> list) {
        log.debug("findWithWildCard");
        Folder start = cur;
        if (path.getLength() > 1) {
            Path pathToStart = path.getParent();
            BaseResource resStart = find(cur, pathToStart);
            if (resStart == null) {
                return result("Couldnt find path: " + pathToStart);
            }
            if (resStart instanceof Folder) {
                start = (Folder) resStart;
            } else {
                return result("is not a folder: " + pathToStart);
            }
        }
        Pattern pattern = null;
        try {
            log.debug("findWithWildCard: compiling " + path.getName());
            pattern = Pattern.compile(path.getName());
        } catch (Exception e) {
            return result("Couldnt compile regular expression: " + path.getName());
        }
        for (Resource ct : start.getChildren()) {
            if (ct instanceof BaseResource) {
                BaseResource res = (BaseResource) ct;
                Matcher m = pattern.matcher(res.getName());
                if (m.matches()) {
                    log.debug("findWithWildCard: matches: " + res.getName());
                    list.add(res);
                } else {
                    log.debug("findWithWildCard: does not match: " + res.getName());
                }
            }
        }
        return null;
    }

    private List<Folder> crawl( Folder cur ) {
        List<Folder> list = new ArrayList<>();
        list.add(cur);
        for( Resource r : cur.getChildren()) {
            if( r instanceof Folder ) {
                list.addAll( crawl((Folder)r));
            }
        }
        return list;
    }
    
    protected Result result(String msg, Exception ex) {
        StringBuilder sb = new StringBuilder(msg);
        formatException(sb, ex);
        return result(sb.toString());
    }

    private void formatException(StringBuilder sb, Throwable ex) {
        sb.append("<br/>");
        sb.append("<h2>").append(ex.getClass().getCanonicalName());
        if( ex.getMessage() != null ) {
            sb.append(ex.getMessage());
        }
        sb.append("</h2>");
        for( StackTraceElement i : ex.getStackTrace()) {
            sb.append(i.getClassName()).append(" :: ").append(i.getMethodName()).append("(").append(i.getLineNumber()).append(")");
            sb.append("<br/>");
        }
        if( ex.getCause() != null ) {
            sb.append("<p>caused by...</p>");
            formatException(sb, ex.getCause());
        }
    }
}
