package com.ettrema.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.PropPatchRequestParser.ParseResult;
import com.bradmcevoy.http.webdav.PropPatchSetter;
import com.ettrema.logging.LogUtils;

/**
 *
 */
public class ClydePropPatchSetter implements PropPatchSetter {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydePropPatchSetter.class);
    private final PropPatchSetter wrapped;

    public ClydePropPatchSetter(PropPatchSetter wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public PropFindResponse setProperties(String href, ParseResult parseResult, Resource r) {
        LogUtils.info(log, "setProperties: delegate to", wrapped.getClass());
        PropFindResponse resp = wrapped.setProperties(href, parseResult, r);
        BaseResource res = (BaseResource) r;
        res.save();
        res.commit();
        return resp;
    }

    @Override
    public boolean supports(Resource r) {
        if (r instanceof BaseResource) {
            return wrapped.supports(r);
        } else {
            return false;
        }
    }
}
