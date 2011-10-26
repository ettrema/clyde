package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.ettrema.utils.LogUtils;

/**
 *
 * @author brad
 */
public class ResourceQueryProcessor {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ResourceQueryProcessor.class);

	public long find(Path path, Templatable startFrom, ResourceConsumer consumer) {
		if (log.isTraceEnabled()) {
			log.trace("find: " + path);
		}
		if (path == null) {
			log.warn("path is null: startFrom: " + startFrom.getHref());
			consumer.onResource(startFrom);
			return 1;
		}
		String[] parts = path.getParts();
		if (!path.isRelative()) {
			startFrom = startFrom.getHost();
		}
		return process(parts, 0, startFrom, consumer, 0);
	}

	private long process(String[] parts, int i, Templatable current, ResourceConsumer consumer, long count) {
		boolean isTerminal = (i >= parts.length - 1);
		String part = parts[i];
		if (log.isTraceEnabled()) {
			if (current instanceof Folder) {
				Folder f = (Folder) current;
				LogUtils.trace(log, "process: folder", f.getUrl());
			} else {
				LogUtils.trace(log, "process: current: ", current.getHref(), " part: ", part, " - isTerminal: ", isTerminal);
			}
		} else {
			if( log.isDebugEnabled()) {
				if( (count % 1000) == 0 ) {
					log.debug("Processed items: " + count);
				}
			}
		}
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
			count = processChildren(current, parts, i, false, consumer, 0);
		} else if (part.equals("**")) {
			// match all resources in this and subdirectories
			count = processChildren(current, parts, i, true, consumer, 0);
		} else {
			// look for child with this name
			if (current instanceof Folder) {
				Folder currentFolder = (Folder) current;
				current = currentFolder.childRes(part);
				if (current != null) {
					if (isTerminal) {
						consumer.onResource(current);
					} else {
						count = process(parts, i + 1, current, consumer, count++);
					}
				}
			}
		}
		return count;
	}

	private long processChildren(Templatable current, String[] parts, int i, boolean recurse, ResourceConsumer consumer, long count) {
		if (current instanceof Folder) {
			if ((i % 1000) == 0) {
				Folder f = (Folder) current;
				LogUtils.info(log, "processChildren: folder", f.getUrl(), "count", count);
			} else {
				LogUtils.trace(log, "processChildren: current", current.getName(), "count", count);
			}
		} else {
			LogUtils.trace(log, "processChildren: current: ", current.getName(), " recurse: ", recurse, "count", count);
		}
		if (current instanceof Folder) {
			boolean isTerminal = (i >= parts.length - 1);
			Folder currentFolder = (Folder) current;
			for (Resource r : currentFolder.getChildren()) {
				if (r instanceof Templatable) {
					Templatable next = (Templatable) r;
					if (isTerminal) {
						LogUtils.trace(log, "matched resource: ", r.getName());
						consumer.onResource(r);
					} else {
						count = process(parts, i + 1, next, consumer, count++);
					}
					if (recurse) {
						count = processChildren(next, parts, i, recurse, consumer, count++);
					}
				}
			}
		} else {
			log.trace(" - not a folder");
		}
		return count;
	}

	public interface ResourceConsumer {

		void onResource(Resource r);
	}
}
