package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.Map;
import org.jdom.Element;

public class DeleteCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeleteCommand.class);
    
    private static final long serialVersionUID = 1L;
    
    public DeleteCommand(Addressable container, Element el) {
        super(container,el);
    }
    
    public DeleteCommand(Addressable container, String name) {
        super(container,name);
    }

    @Override
    public int getSignificance() {
        return 10;
    }

    
    
    @Override
    public String render(RenderContext rc) {
        return "<button type='submit' name='" + name + "' value='" + name + "' onclick=\"return confirm('Are you sure you want to delete " + rc.getTargetPage().getName() + "?')\" />" + name + "</button>";
    }
    
    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        // TODO: validate
        String s = parameters.get(this.getName());
        if( s == null ) {
            return null; // not this command
        }
        return doProcess(rc,parameters,files);
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug("doing delete");
        PermissionChecker permissionChecker = requestContext().get( PermissionChecker.class);

        if( !permissionChecker.hasRole( Role.AUTHOR, rc.getTargetPage(), RequestParams.current().getAuth()) ) {
            throw new RuntimeException( "no permission to delete");
        }

        Templatable tr = rc.getTargetPage();
        if( tr instanceof BaseResource ) {
            BaseResource f = (BaseResource)tr;
            String redirectTo;
            if( f.getParent().getIndexPage() == tr ) {
                redirectTo = f.getWeb().getHref();
            } else {
                redirectTo = f.getParent().getHref();
            }
            doDelete(f);
            commit();
            log.debug("  - deleted. redireting to: " + redirectTo);
            return redirectTo;
        } else {
            log.debug("** not a deletable item: " + tr.getClass().getName());
            return null;
        }
    }



    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    private void doDelete( BaseResource f ) {
        try {
            f.delete();
        } catch( NotAuthorizedException ex ) {
            throw new RuntimeException( ex );
        } catch( ConflictException ex ) {
            throw new RuntimeException( ex );
        } catch( BadRequestException ex ) {
            throw new RuntimeException( ex );
        }
    }
    

}
