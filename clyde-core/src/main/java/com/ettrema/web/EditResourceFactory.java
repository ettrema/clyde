package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class EditResourceFactory extends CommonResourceFactory {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditResourceFactory.class);

    private final ResourceFactory next;

    public EditResourceFactory(ResourceFactory next, HostFinder hostFinder) {
        super(hostFinder );
        this.next = next;
    }
        
    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException, BadRequestException{
        Path path = Path.path(url);        
        if( EditPage.isEditPath(path)) {
            Path editee = EditPage.getEditeePath(path);        
            Resource res = next.getResource(host,editee.toString());
            if( res == null ) {
                return null;
            } else {
                if( res instanceof EditableResource ) {
                    EditableResource er = (EditableResource) res;
                    Resource editPage = er.getEditPage();
					return editPage;
                } else {
                    return null;
                }
            }
        } else {
            Resource res = next.getResource(host, url);
            return res;
        }
    }
    
}
