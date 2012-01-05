package com.ettrema.event;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.RenderContext;

/**
 *
 * @author brad
 */
public interface ClydeEventDispatcher extends ResourceEventDispatcher{
    void beforeRender(CommonTemplated ct, RenderContext child);
}
