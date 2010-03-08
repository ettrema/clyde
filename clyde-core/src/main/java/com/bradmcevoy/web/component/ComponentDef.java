package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.*;
import java.util.Map;

public interface ComponentDef extends Component {

    public boolean validate(ComponentValue c,RenderContext rc);
    
    @Override
    String getName();
    
    String render(ComponentValue c,RenderContext rc);

    String renderEdit(ComponentValue c,RenderContext rc);
    
    void onPreProcess(ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files);

    ComponentValue createComponentValue(Templatable newRes);
    
    Object parseValue(ComponentValue cv, Templatable ct, String s);

    /**
     * Used for persistence to xml. Symmmetrical with parseValue
     * @param v
     * @return
     */
    public String formatValue(Object v);
    
    /**
     * Called by clients when they directly change a componentvalue's value
     * 
     * @param cv
     */
    public void changedValue(ComponentValue cv);

    /**
     * 
     * @return - the most specific class or interface that allowable values can be
     */
    public Class getValueClass();
}
