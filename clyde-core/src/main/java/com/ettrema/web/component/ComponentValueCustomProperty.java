package com.ettrema.web.component;

import com.bradmcevoy.http.CustomProperty;
import com.ettrema.web.CommonTemplated;

/**
 *
 * @author brad
 */
public class ComponentValueCustomProperty implements CustomProperty {

    private final ComponentValue value;
    private final CommonTemplated page;
    private final ComponentDef componentDef;

    public ComponentValueCustomProperty( ComponentValue value, CommonTemplated page ) {
        this.value = value;
        this.page = page;
        this.componentDef = page.getTemplate().getComponentDef( value.getName() );
    }

    @Override
    public Object getTypedValue() {
        return value.getValue();
    }

    @Override
    public String getFormattedValue() {
        return componentDef.formatValue( value );
    }

    @Override
    public void setFormattedValue( String val ) {
        Object o = componentDef.parseValue( value, page, val );
        value.setValue( o );
    }

    @Override
    public Class getValueClass() {
        if( value.getValue() != null ) {
            return value.getValue().getClass();
        } else {
            return componentDef.getValueClass();
        }
    }
}
