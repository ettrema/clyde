package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class ResourceQueryProcessor {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ResourceQueryProcessor.class);

    public void find(Path path, Templatable startFrom, ResourceConsumer consumer) {
        if (log.isTraceEnabled()) {
            log.trace("find: " + path);
        }
        if( path == null ) {
            log.warn("path is null: startFrom: " + startFrom.getHref());
            consumer.onResource(startFrom);
            return ;
        }
        String[] parts = path.getParts();
        if (!path.isRelative()) {
            startFrom = startFrom.getHost();
        }
        process(parts, 0, startFrom, consumer);
    }

    private void process(String[] parts, int i, Templatable current, ResourceConsumer consumer) {
        boolean isTerminal = (i >= parts.length - 1);
        String part = parts[i];
        log.trace("process: current: " + current.getHref() + " part: " + part + " - isTerminal: " + isTerminal);
        if (part.equals(".")) {
            if (isTerminal) {
                consumer.onResource(current);
            }

        } else if (part.equals("..")) {
            current = current.getParent();
            if (isTerminal) {
                consumer.onResource(current);
            }

        } else if (part.equals("*")) {
            // match all resources in current directory
            processChildren(current, parts, i, false, consumer);
        } else if (part.equals("**")) {
            // match all resources in this and subdirectories
            processChildren(current, parts, i, true, consumer);
        } else {
            // look for child with this name
            if (current instanceof Folder) {
                Folder currentFolder = (Folder) current;
                current = currentFolder.childRes(part);
                if (current != null) {
                    if (isTerminal) {
                        consumer.onResource(current);
                    } else {
                        process(parts, i + 1, current, consumer);
                    }
                }
            }
        }

    }

    private void processChildren(Templatable current, String[] parts, int i, boolean recurse, ResourceConsumer consumer) {
        log.trace("processChildren: " + current.getName() + " - recurse: " + recurse);
        if (current instanceof Folder) {
            boolean isTerminal = (i >= parts.length - 1);
            Folder currentFolder = (Folder) current;
            for (Resource r : currentFolder.getChildren()) {
                if (r instanceof Templatable) {
                    Templatable next = (Templatable) r;
                    if (isTerminal) {
                        log.trace("matched resource: " + r.getName());
                        consumer.onResource(r);
                    } else {
                        process(parts, i + 1, next, consumer);
                    }
                    if (recurse) {
                        processChildren(next, parts, i, recurse, consumer);
                    }
                }
            }
        } else {
            log.trace(" - not a folder");
        }
    }

    public interface ResourceConsumer {

        void onResource(Resource r);
    }
}
