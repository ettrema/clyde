package com.ettrema.forms;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.CommonTemplated;
import java.util.Map;

/**
 * Called from CommonTemplates.processForm
 *
 * @author brad
 */
public interface FormProcessor {
	/**
	 * 
	 * @param parameters
	 * @param files
	 * @return - null, or a redirect url
	 * @throws NotAuthorizedException 
	 */
	String processForm(CommonTemplated target,Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException;
}
