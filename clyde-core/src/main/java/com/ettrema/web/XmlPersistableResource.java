
package com.ettrema.web;

import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Map;
import org.jdom.Element;


public interface XmlPersistableResource extends DigestResource {
    /**
     * Update the resource with the xml repsentation in el
     *
     * @param el
     * @param params
     */
    public void loadFromXml(Element el,Map<String, String> params);

    /**
     * Get an XML representation of this resource
     *
     * @param el
     * @param params
     * @return
     */
    public Element toXml(Element el,Map<String, String> params);

    public void save();

    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException;
    
    public String getHref();
}
