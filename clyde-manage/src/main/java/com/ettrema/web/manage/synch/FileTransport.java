package com.bradmcevoy.web.manage.synch;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author brad
 */
public interface FileTransport {
	
	void put(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException, IOException;
	
	void delete(File f, File root) throws NotAuthorizedException, ConflictException, BadRequestException;
	
	/**
	 * Is the file changed or newly created relative to the repository
	 * 
	 * @param f
	 * @param root
	 * @return 
	 */
	boolean isNewOrUpdated(File f, File root);
}
