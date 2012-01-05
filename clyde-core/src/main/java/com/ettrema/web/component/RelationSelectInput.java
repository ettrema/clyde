package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Component;
import com.ettrema.web.Folder;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import com.ettrema.web.Templatable;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class RelationSelectInput extends CommonComponent implements Addressable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Text.class);
    private static final long serialVersionUID = 1L;

    protected Addressable container;

    protected String name;
    protected boolean required;
    protected String relationName;
    protected String selectFromFolder;
    protected String selectTemplate;

    public RelationSelectInput(Addressable container,String name) {
        this.container = container;
        this.name = name;
    }


    public RelationSelectInput(Addressable container, Element el) {
        this.container = container;
        fromXml(el);
    }

    @Override
    public void init( Addressable container ) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public boolean validate( RenderContext rc ) {
        // todo
        return true;
    }

    @Override
    public String render( RenderContext rc ) {
        BaseResource res = (BaseResource) rc.getTargetPage();
        BaseResource relation = res.getRelation( relationName );
        return relation.getLink();
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        Templatable selectFrom = rc.getTarget().find( selectFromFolder );
        if( selectFrom == null ) {
            return "Error: couldnt find folder: " + selectFromFolder;
        } else if( selectFrom instanceof Folder) {
            StringBuffer sb = new StringBuffer();
            sb.append( "<select name='").append( name).append( "'>");
            Folder fSelectFrom = (Folder) selectFrom;
            for(Templatable ct : fSelectFrom.getChildren( selectTemplate)) {
                if( ct instanceof BaseResource){
                    BaseResource res = (BaseResource) ct;
                    sb.append( "<option value='").append( res.getPath().toString() ).append( "'>")
                            .append( res.getTitle()).append( "</option>");
                }
            }
            sb.append( "</select>");
            return sb.toString();
        } else {
            return "Error: not a folder: " + selectFromFolder;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        BaseResource target = (BaseResource) rc.getTargetPage();
        String paramName = getPath(rc).toString();
        if( !parameters.containsKey(paramName) ) return null;

        String s = parameters.get(paramName);
        Path p = Path.path( s);
        RequestParams.current().attributes.put(this.getName(),p);

        BaseResource to = (BaseResource) target.find( p );

        target.createRelationship( relationName, to);
        return null;
    }

    @Override
    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    public void fromXml(Element el) {
        name = el.getAttributeValue("name");
        required = InitUtils.getBoolean(el, "required");
        String s = InitUtils.getValue(el);
    }

    @Override
    public Element toXml(Addressable container,Element el) {
        Element e2 = new Element("component");
        el.addContent(e2);
        e2.setAttribute("name",name);
        e2.setAttribute("class",this.getClass().getName());
        InitUtils.setBoolean(e2, "required", required);
//        String s = getFormattedValue();
//        if( s != null ) {
//            if( s.contains( "<![CDATA")) {
//                e2.addContent( s );
//            } else {
//                CDATA data = new CDATA(s);
//                e2.addContent(data);
//            }
//        }
        return e2;
    }

    public String getHtmlId() {
        String s = getPath().toString();
        if( s.startsWith("/")) s = s.substring(1);
        s = s.replaceAll("/", "-");
        s = s.replace('.', '_');
        return s;
    }

    @Override
    public Path getPath() {
        return ComponentUtils.findPath((Component)this);
    }

}
