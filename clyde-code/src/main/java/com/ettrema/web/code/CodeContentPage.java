package com.ettrema.web.code;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.Replaceable;
import com.ettrema.web.code.content.CodeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Is the content of the underlying physical resource. For binary content, this
 * will be exactly the same as what is persisted.
 *
 * For templated resources, this will be an xml (or xhtml) representation of the
 * content within the templated resource.
 *
 * @author brad
 */
public class CodeContentPage extends AbstractCodeResource<GetableResource> implements GetableResource, Replaceable, MoveableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CodeContentPage.class);

    public CodeContentPage(CodeResourceFactory rf, String name, GetableResource wrapped) {
        super(rf, name, wrapped);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        ContentTypeHandler cth = rf.getContentTypeHandler(wrapped);
        if (cth != null) {
            log.trace("generate content for: " + wrapped.getClass() + " with " + cth.getClass());
            cth.generateContent(out, wrapped);
        } else {
            log.warn("No content type handler for: " + wrapped.getClass());
        }
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return wrapped.getContentType(accepts);
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public void replaceContent(InputStream in, Long l) {
        log.trace("replaceContent");
        ContentTypeHandler cth = rf.getContentTypeHandler(wrapped);
        if (cth != null) {
            cth.replaceContent(in, l, wrapped);
            CodeUtils.commit();
        } else {
            log.warn("Couldnt get a content type handler!!! " + wrapped.getClass());
        }
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        if (rDest instanceof CodeFolder) {
            if (this.wrapped instanceof MoveableResource) {
                MoveableResource source = (MoveableResource) this.wrapped;
                CodeFolder cfDest = (CodeFolder) rDest;
                source.moveTo(cfDest.wrapped, name);
            } else {
                throw new ConflictException(rDest);
            }
        } else {
            throw new ConflictException(rDest);
        }
    }
}
