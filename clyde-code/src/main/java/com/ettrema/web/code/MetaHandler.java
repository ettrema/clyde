package com.ettrema.web.code;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public interface MetaHandler<T extends Resource> {

    boolean supports(Resource r);

    /**
     * Xml root element name which this handler can produce and parse
     * 
     * @return
     */
    String getAlias();

    Class getInstanceType();

    Element toXml(T r);

    /**
     * Create a new instance of the resource from xml meta
     *
     * @param parent
     * @param d
     * @param name - the name of the actual resource to create (does not include meta suffix)
     * @return
     */
    T createFromXml(CollectionResource parent, Element d, String name);

    /**
     * Update an existing resource from xml meta
     *
     * @param d
     */
    void updateFromXml(T r, Element d);

}
