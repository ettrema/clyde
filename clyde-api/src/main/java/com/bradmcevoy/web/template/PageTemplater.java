package com.bradmcevoy.web.template;

import com.bradmcevoy.http.Resource;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author brad
 */
public interface PageTemplater {
    void render( String templateName, OutputStream out, Map<String, String> params );
    void render(Resource resource, OutputStream out, Map<String, String> params);
}
