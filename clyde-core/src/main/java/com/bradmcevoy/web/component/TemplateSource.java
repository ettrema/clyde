package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.Templatable;

public class TemplateSource {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TemplateSource.class );

    final RenderContext rc;
    final Path p;
    final Object value;
    final AbstractInput input;

    public TemplateSource(RenderContext rc, Path p, Object value, AbstractInput input) {
        this.rc = rc;
        this.p = p;
        this.value = value;
        this.input = input;
    }

    public Path getPath() {
        return p;
    }

    public Object getValue() {
        return value;
    }

    public String getFormattedValue() {
        Object o = getValue();
        if (o == null) {
            return "";
        }
        return o.toString();
    }

    public AbstractInput getInput() {
        return input;
    }

    public Templatable getPage() {
        if( rc == null ) {
            log.warn("no render context, so can't get page");
            return null;
        } else {
            return rc.getTargetPage();
        }
    }

    public Auth getAuth() {
        return RequestParams.current().getAuth();
    }

    public String getActualHref() {
        return RequestParams.current().href;
    }


    
    public boolean isEditMode() {
        return rc.editMode;
    }
}
