package com.ettrema.web.template;

import com.bradmcevoy.http.Resource;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author brad
 */
public interface PageTemplater {
    void render( String templateName, OutputStream out, Map<String, String> params, Map<String,Object> attrs );
    void render(Resource resource, OutputStream out, Map<String, String> params);
}
