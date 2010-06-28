package com.bradmcevoy.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.PropPatchRequestParser.ParseResult;
import com.bradmcevoy.http.webdav.PropPatchSetter;
import java.util.List;

/**
 *
 */
public class ClydePropPatchSetter implements PropPatchSetter{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydePropPatchSetter.class );

    private final PropPatchSetter wrapped;

    public ClydePropPatchSetter(PropPatchSetter wrapped) {
        this.wrapped = wrapped;
    }

    public List<PropFindResponse> setProperties(String href, ParseResult parseResult, Resource r) {
        log.debug("setProperties");
        List<PropFindResponse> list = wrapped.setProperties(href, parseResult, r);
        BaseResource res = (BaseResource) r;
        res.save();
        res.commit();
        return list;
    }

    public boolean supports(Resource r) {
        if( r instanceof BaseResource) {
            return wrapped.supports(r);
        } else {
            return false;
        }
    }

}