
package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Resource;
import com.ettrema.media.ThumbGeneratorService;
import com.ettrema.vfs.VfsSession;
import com.ettrema.web.Folder;
import com.ettrema.web.ImageFile;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

public class GenerateThumbsCommand extends Command {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SaveCommand.class);
    
    private static final long serialVersionUID = 1L;
    
    public GenerateThumbsCommand(Addressable container, Element el) {
        super(container,el);
    }

    public GenerateThumbsCommand(Addressable container, String name) {
        super(container,name);
    }
    
    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        if( !isApplicable(parameters) ) {
            return null; // not this command
        }
        doProcess(rc, parameters, files);
        return null;
    }

    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    protected boolean isApplicable(Map<String, String> parameters) {
        String s = parameters.get(this.getName());
        return ( s != null );

    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug( "doProcess");
        Templatable tr = rc.getTargetPage();
        if( tr instanceof Folder ) {
			ThumbGeneratorService thumbGeneratorService = _(ThumbGeneratorService.class);
			VfsSession vfs = _(VfsSession.class);
            Folder folder = (Folder)tr;
            for( Resource res : folder.getChildren() ) {
                if( res instanceof ImageFile ) {
                    ImageFile img = (ImageFile) res;
                    thumbGeneratorService.doGeneration(img, vfs);
                }
            }
            commit();
            return null;
        } else {
            throw new RuntimeException("Cant generate thumbs for a page: " + tr.getClass());
        }
    }
    

}
