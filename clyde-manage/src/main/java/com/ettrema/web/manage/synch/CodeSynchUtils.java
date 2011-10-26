package com.bradmcevoy.web.manage.synch;

import java.io.File;

/**
 *
 * @author brad
 */
public class CodeSynchUtils {

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CodeSynchUtils.class);

	/**
	 * Finds the relative path of f to root, and prefixes with the code root path
	 * 
	 * Uses http friendly seperators, not local file seperators
	 * 
	 * @param f
	 * @param root
	 * @return 
	 */
	public static String toCodePath(File f, File root) {
		String s = f.getAbsolutePath();
		if (s.startsWith(root.getAbsolutePath())) {
			s = s.replace(root.getAbsolutePath(), "");
			s = s.replace("\\", "/");
			s = "/_code" + s;
			return s;
		} else {
			log.warn("File does not begin with the root path. File:" + f.getAbsolutePath() + " root: " + root.getAbsolutePath());
			return null;
		}
	}
	
	/**
	 * Finds the relative path of f to root
	 * 
	 * Uses http friendly seperators, not local file seperators
	 * 
	 * @param f
	 * @param root
	 * @return 
	 */	
	public static String toPath(File f, File root) {
		String s = f.getAbsolutePath();
		if (s.startsWith(root.getAbsolutePath())) {
			s = s.replace(root.getAbsolutePath(), "");
			s = s.replace("\\", "/");
			return s;
		} else {
			log.warn("File does not begin with the root path. File:" + f.getAbsolutePath() + " root: " + root.getAbsolutePath());
			return null;
		}
	}	
	


	public static File toMetaFile(File f) {
		if (f.getName().endsWith(".meta.xml")) {
			return f;
		} else {
			String metaName = f.getName() + ".meta.xml";
			return new File(f.getParentFile(), metaName);

		}
	}

	public static File toContentFile(File f) {
		if (f.getName().endsWith(".meta.xml")) {
			String contentName = f.getName().replace(".meta.xml", "");
			return new File(f.getParentFile(), contentName);
		} else {
			return f;
		}
	}
}
