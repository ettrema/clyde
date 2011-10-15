package com.bradmcevoy.web;

import com.bradmcevoy.utils.LogUtils;

/**
 * Loads classes to use as templates
 *
 * @author bradm
 */
public class ClasspathTemplateManager implements TemplateManager {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClasspathTemplateManager.class);
	
	private final String packageName;

	public ClasspathTemplateManager(String packageName) {
		this.packageName = packageName;
	}
	
	
	
	@Override
	public ITemplate lookup(String templateName, Folder web) {
		String className = packageName + "." + templateName;
		Class c;
		try {
			c = Class.forName(className);
		} catch (ClassNotFoundException ex) {
			LogUtils.trace(log, "Couldnt find", className);
			return null;
		}
		try {
			Object template = c.newInstance();
			if( template instanceof ITemplate) {
				return (ITemplate) template;
			} else {
				throw new RuntimeException("Class is not a template: " + className + " is a: " + template.getClass().getCanonicalName());
			}
		} catch (InstantiationException ex) {
			throw new RuntimeException(className, ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(className, ex);
		}
	}
	
}
