package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.ettrema.web.*;
import java.util.Map;
import org.jdom.Element;

public interface ComponentDef extends Component {

    boolean validate(ComponentValue c,RenderContext rc);
    
    @Override
    String getName();
    
    String render(ComponentValue c,RenderContext rc);

    String renderEdit(ComponentValue c,RenderContext rc);
    
    /**
     * Called prior to validation, this should ensure that any changes
     * made by the user are loaded
     * 
     * @param componentValue
     * @param rc
     * @param parameters
     * @param files 
     */
    void onPreProcess(ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files);

    ComponentValue createComponentValue(Templatable newRes);

    /**
     * Parse the given textual representation of the value
     *
     * @param cv
     * @param ct
     * @param s
     * @return
     */
    Object parseValue(ComponentValue cv, Templatable ct, String s);

    /**
     * Parse the given XML representation of the value
     *
     * @param cv
     * @param ct
     * @param elValue
     * @return
     */
    Object parseValue(ComponentValue cv, Templatable ct, Element elValue);

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
