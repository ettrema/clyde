package com.bradmcevoy.web;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.HtmlInput;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@BeanPropertyResource("clyde")
public class Page extends File implements Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Page.class);
    private static final long serialVersionUID = 1L;

    public Page(Folder parentFolder, String name) {
        super("text/html", parentFolder, name);

    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new Page(parent, newName);
    }

    @Override
    protected void initComponents() {
        super.initComponents();
    }

    @Override
    void setContent( InputStream in) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            StreamUtils.readTo(in, bout);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        }
        HtmlInput root = new HtmlInput(this, "root");
        root.setValue(bout.toString());
        componentMap.add(root);
        save();
    }

    @Override
    public boolean is(String type) {
        if (type == null) {
            return false;
        }
        if (super.is(type)) {
            return true;
        } else {
            return (type.equals("html") || type.equals("page"));
        }
    }


    @Override
    public void replaceContent(InputStream in, Long arg1) {
        log.debug("replaceContent");
        ReplaceableHtmlParser parser = new ReplaceableHtmlParserImpl();
        ITemplate template = this.getTemplate();
        ComponentDefMap defs;
        Map<String, String> mapOfVals;
        if( template == null ) {
            log.warn("Cant replace content, no template");
        } else {
            defs = template.getComponentDefs();
            if( defs != null ) {
                if( defs.size() > 0 ) {
                    try {
                        Set<String> names = defs.keySet();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        StreamUtils.readTo(in, out);
                        mapOfVals = parser.parse(out.toString(), names);
                        log.debug("found values: " + mapOfVals.size());
                        for(Map.Entry<String,String> entry : mapOfVals.entrySet()) {
                            ComponentValue cv = this.getValues().get(entry.getKey());
                            if( cv != null ) {
                                cv.value = entry.getValue();
                            } else {
                                log.warn("no value: " + entry.getKey());
                            }
                        }
                        log.debug("saving");
                        this.save();
                        this.commit();
                    } catch (ReadingException ex) {
                        log.warn("reading exception");
                    } catch (WritingException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    log.warn("empty component defs, cant replace. template: " + template.getHref());
                }
            } else {
                log.warn("no component defs map, can't replace content");
            }
        }
    }
}
