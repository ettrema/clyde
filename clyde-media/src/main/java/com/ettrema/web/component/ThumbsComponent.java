package com.ettrema.web.component;

import com.ettrema.web.Thumb;
import java.util.List;
import org.jdom.Element;

public class ThumbsComponent extends AbstractInput<List<Thumb>>{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThumbsComponent.class);
    private static final long serialVersionUID = 1L;

    public ThumbsComponent(Addressable container,String name) {
        super(container,name);
        setValue(null);
    }

    public ThumbsComponent(Addressable container,String name, List<Thumb> value) {
        this(container,name);
        setValue(value);
    }
    
    public ThumbsComponent(Addressable container, Element el) {
        super(container,el);
    }
    

    @Override
    protected String editTemplate() {
        String template = "<input type='text' name='${path}' value='${formattedValue}' size='${input.cols}' />";
        return template;
    }

    @Override
    protected List<Thumb> parse(String s) {
        List<Thumb> list = Thumb.parseThumbs(s);
        return list;
    }
    
    @Override
    public String getFormattedValue() {
        List<Thumb> v = getValue();
        return Thumb.format(v);
    }    

}
