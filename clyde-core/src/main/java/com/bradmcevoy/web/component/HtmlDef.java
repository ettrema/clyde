package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.*;
import java.util.Map;
import org.jdom.Element;

public class HtmlDef extends TextDef {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HtmlDef.class);
    private static final long serialVersionUID = 1L;
    
    
    Text toolbarSet = new Text(this,"toolbarSet");
    
    public HtmlDef(Addressable container,String name) {
        super(container,name);
    }

    public HtmlDef(Addressable container,String name, int cols, int rows) {
        super(container,name,cols,rows);
    }

    public HtmlDef(Addressable container, Element el) {
        super(container,el);
        this.toolbarSet.setValue( InitUtils.getValue(el,"toolbarSet"));
    }

    @Override
    public Element toXml(Addressable container,Element el) {
        Element e2 = super.toXml(container,el);
        InitUtils.setString(e2,toolbarSet);
        return e2;
    }
    
    
    
    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) { 
        super.onPreProcess(rc,parameters,files);
        if( toolbarSet != null )
            toolbarSet.onPreProcess(rc, parameters, files);
    }
    
    public Integer getWidth() {
        if( cols == null ) return null;
        if( cols.getValue() == null ) return null;
        return cols.getValue() * 8;
    }
    
    public Integer getHeight() {
        if( rows == null ) return null;
        if( rows.getValue() == null ) return null;
        return rows.getValue() * 18;
    }
    
    public String getToolbarSetName() {
        if( toolbarSet == null || toolbarSet.getValue() == null || toolbarSet.getValue().length() == 0 ) {
            return "Basic";
        } else {
            return toolbarSet.getValue();
        }
    }

    public void setToolbarSetName(String s) {
        if( toolbarSet == null ) {
            toolbarSet = new Text(this,"toolbarSet");
        }
        toolbarSet.setValue( s );
    }
    
    @Override
    protected String editChildTemplate() {
//        return "<textarea name='@{path}' rows='@{comp.rows}' cols='@{comp.cols}'>@{value}</textarea>";
        return
                "<textarea name='${path}' id='${path}' cols='${def.cols}' rows='${def.rows}'  wrap='OFF'>${value}</textarea>\n" +
                "<script type='text/javascript'>\n" +
                "create( '${path}',${def.width},${def.height},'${def.toolbarSetName}');\n" +
                "</script>\n";
    }
    
    @Override    
    public String renderEdit(RenderContext rc) {
        String s = super.renderEdit(rc);
        s = s + "<br/>";
        s = s + "rows: " + rows.renderEdit(rc);
        s = s + "<br/>";
        s = s + "cols: " + cols.renderEdit(rc);
        return s;
    }    
    
}
