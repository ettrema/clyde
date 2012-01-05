
package com.ettrema.web.component;

import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
//import com.bradmcevoy.media.ImageFile;
import com.ettrema.web.RenderContext;
import com.ettrema.web.SubPage;
import com.ettrema.web.Web;
import org.jdom.Element;

public class ThemeSelect extends AbstractInput<String> {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThemeSelect.class);
    private static final long serialVersionUID = 1L;
    
    public ThemeSelect(Addressable container, String name) {
        super(container,name);
    }

    public ThemeSelect(Addressable container, Element el) {
        super(container,el);
    }
    
    @Override
    protected String editTemplate() {
        StringBuffer sb = new StringBuffer();
        sb.append("<input type='text' name='${path}' id='${input.htmlId}' value='' />");
        sb.append("<br/>");
        Web web = findWeb(this.container);
        if( web != null ) {
            Folder themes = web.getThemes();
            if( themes != null ) {
                for( Folder theme : themes.getSubFolders() ) {  // TODO: configurable source
                    BaseResource res = theme.childRes("normal.html");
                    if( res != null ) {
                        String previewName = "preview.jpg";
                        BaseResource resPreview = theme.childRes(previewName);
                        String js = "document.getElementById(\"" + this.getHtmlId() + "\").value = \"" + theme.getName() + "\"; return false;";
//                        if( resPreview != null && resPreview instanceof ImageFile) {
//                            ImageFile img = (ImageFile) resPreview;
//                            sb.append("<a href='#' onclick='" + js + "'>").append(img.getImg()).append("</a>");
//                        } else {
//                            sb.append("<a href='#' onclick='" + js + "'>").append(theme.getName()).append("</a>");
//                        }
                        sb.append("<br/>");
                    }                    
                }
            }
        }
        return sb.toString();
    }

//    @Override
//    protected String editTemplate() {
//        StringBuffer sb = new StringBuffer();
//        sb.append("<select name='${path}'>");
//        Web web = findWeb(this.container);
//        if( web != null ) {
//            Folder themes = web.getThemes();
//            if( themes != null ) {
//                for( Folder theme : themes.getSubFolders() ) {  // TODO: configurable source
//                    sb.append("<option value='").append(theme.getName()).append("'");
//                    if( theme.getName().equals(getValue())) {
//                        sb.append(" selected ");
//                    }
//                    sb.append(">").append(theme.getName()).append("</option>");
//                }
//            }
//        }
//        sb.append("</select>");
//        return sb.toString();
//    }
    
    @Override
    protected String parse(String s) {
        return s;
    }

    private Web findWeb(Addressable container) {
        if( container instanceof BaseResource ) {
            BaseResource res = (BaseResource) container;
            return res.getWeb();
        } else if( container instanceof Command ) {
            Command cmd = (Command) container;
            return findWeb(cmd.container);
        } else if( container instanceof SubPage ) {
            SubPage p = (SubPage) container;
            return p.getWeb();
        } else {
            throw new RuntimeException("Unhandled container type: " + container.getClass());
        }
    }

    @Override
    public boolean validate(RenderContext rc) {
        boolean b = super.validate(rc);
        if( !b ) {
            log.debug("validation failed on themeselect. value: " + this.getValue());
        }
        return b;
    }    
    
}
