package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.RenderContext;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Element;
import org.mvel.TemplateInterpreter;

/**
 *
 */
public class CommentCommand extends Command implements Serializable{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommentCommand.class);

    private static final long serialVersionUID = 1L;

    private String template;

    public CommentCommand(Addressable container, Element el) {
        super(container, el);
    }

    public CommentCommand(Addressable container, String name) {
        super(container, name);
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug("inProces");
        if(!isApplicable(parameters)) {
            return null;
        }
        if( !validate(rc)) {
            return null;
        }
        String text = parameters.get("comment");
        String user = parameters.get("user");
        BaseResource res = (BaseResource) rc.getTargetPage();
        Comment comment = new Comment(new Date(), text, user);
        res.getNameNode().add(comment.getName(), comment);
        comment.save();
        commit();
        return null;

    }

    @Override
    public boolean validate(RenderContext rc) {
        return true; // todo
    }


    protected boolean isApplicable(Map<String, String> parameters) {
        String s = parameters.get(this.getName());
        return (s != null);
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return doProcess(rc, parameters, files);
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        Content content = new CDATA(template);
        e2.setContent(content);
    }

    @Override
    public void fromXml(Element el) {
        super.fromXml(el);
        template = el.getText();
    }



    @Override
    public String render(RenderContext rc) {
        StringBuffer sb = new StringBuffer();
        BaseResource res = (BaseResource) rc.getTargetPage();
        for( NameNode nn : res.getNameNode().children() ) {
            if( Comment.class.equals(nn.getDataClass()) ) {
                Object oComment = nn.getData();
                if( oComment instanceof Comment ) {
                    String s = renderComment(oComment);
                    sb.append(s);
                }
            }
        }
        return sb.toString();
    }

    private String renderComment(Object oComment) {
//        Map map = new HashMap();
//        map.put("comment", oComment);
        return TemplateInterpreter.evalToString(template, oComment);
    }



}
