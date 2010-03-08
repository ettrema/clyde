
package com.bradmcevoy.web;

import com.bradmcevoy.web.component.Command;
import com.bradmcevoy.web.component.DeleteCommand;
import java.util.Comparator;

class ComponentComparator implements Comparator<Component> {

    public ComponentComparator() {
    }

    @Override
    public int compare(Component o1, Component o2) {
        if( o1 instanceof Command ) {
            if( o2 instanceof Command ) {
                Command c1 = (Command) o1;
                Command c2 = (Command) o2;
                if( c1.getSignificance() < c2.getSignificance() ) {
                    return -1;
                } else if( c1.getSignificance() > c2.getSignificance() ) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        } else if( o2 instanceof DeleteCommand ) {
            return -1;
        } else {
            return o1.getName().compareTo(o2.getName());
        }
    }

}
