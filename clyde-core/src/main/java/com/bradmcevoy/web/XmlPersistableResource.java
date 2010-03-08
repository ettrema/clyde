
package com.bradmcevoy.web;

import com.bradmcevoy.http.DigestResource;
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

    public void delete();
    
    public String getHref();
}
