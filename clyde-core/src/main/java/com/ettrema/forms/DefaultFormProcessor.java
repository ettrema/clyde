package com.ettrema.forms;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.CommonTemplated;
import java.util.Map;

/**
 *
 * @author brad
 */
public class DefaultFormProcessor implements FormProcessor {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultFormProcessor.class);
	
	@Override
	public String processForm(CommonTemplated target, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
		log.info("processForm");
		String redirect = target.doOnPost(parameters, files); 
		if (redirect != null) {
			return redirect;
		}
		target.preProcess(null, parameters, files);
		String s = target.process(null, parameters, files);
		return s;
	}
	
}
