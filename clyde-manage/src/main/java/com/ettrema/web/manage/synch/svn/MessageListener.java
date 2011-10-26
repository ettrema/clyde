package com.bradmcevoy.web.manage.synch.svn;

/**
 *
 * @author brad
 */
public interface MessageListener {
	void onMessage(Class source, String msg);
}
