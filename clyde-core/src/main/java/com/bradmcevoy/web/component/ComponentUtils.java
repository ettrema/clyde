
package com.bradmcevoy.web.component;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import java.lang.reflect.Field;
import java.util.Map;

public class ComponentUtils {
    public static boolean validateComponents(Object target, RenderContext rc) {
        Map<String,Field> fields = InitUtils.componentFields(target);
        boolean b = true;
        for( Field f : fields.values() ) {
            Component c = InitUtils.getComponent(f, target);
            b = b && c.validate(rc);
        }
        return b;
    }
    
    public static boolean validatePage(CommonTemplated page, RenderContext rc) {
            boolean ok = true;
            for( Component v : page.allComponents() ) {
                if( v instanceof Command ) {
                    // ignore
                } else {
                    ok = ok && v.validate(rc);
                }
            }
            return ok;        
    }
}
