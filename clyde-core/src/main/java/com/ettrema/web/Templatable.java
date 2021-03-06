package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.web.CommonTemplated.Params;
import com.ettrema.web.component.Addressable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public interface Templatable extends Resource, Addressable, Comparable<Resource>, ComponentContainer {

    List<WebResource> getWebResources();
    
    void sendContent( OutputStream out, Range range, Map<String,String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException;

    String getTemplateName();

    ITemplate getTemplate();

    Collection<Component> allComponents();
    
    Component getComponent( String paramName, boolean includeValues );

    boolean is( String type );

    ComponentValueMap getValues();

    ComponentMap getComponents();

    void preProcess( RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files );

    /**
     * Process a form
     *
     * @param rcChild
     * @param parameters
     * @param files
     * @return
     * @throws NotAuthorizedException
     */
    String process( RenderContext rcChild,Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException;

	/**
	 * Return the fully qualified address of the resource
	 * 
	 * @return 
	 */
    String getHref();
	
	/**
	 * Return the path of the resouce, not including host name, port or protocol
	 * 
	 * @return 
	 */
	String getUrl();

    Web getWeb();

    Host getHost();

    Folder getParentFolder();

    Templatable find( Path path );

    Templatable getParent();

    Params getParams();

    String getContentType( String accepts );

    Date getCreateDate();
}
