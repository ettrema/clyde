package com.ettrema.event;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.RenderContext;

/**
 *
 * @author brad
 */
public interface ClydeEventDispatcher extends ResourceEventDispatcher{
    void beforeRender(CommonTemplated ct, RenderContext child);
}
