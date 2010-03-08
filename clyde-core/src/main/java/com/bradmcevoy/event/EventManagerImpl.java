package com.bradmcevoy.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class EventManagerImpl implements EventManager{

    private final Map<Class,List<EventListener>> listenersMap = new HashMap<Class, List<EventListener>>();

    @Override
    public void fireEvent( Event e ) {
        List<EventListener> list = listenersMap.get( e.getClass());
        if( list == null ) return ;
        for( EventListener l : list) {
            l.onEvent( e );
        }
    }

    @Override
    public synchronized <T extends Event> void registerEventListener( EventListener l, Class<T> c ) {
        List<EventListener> list = listenersMap.get( c );
        if( list == null ) {
            list = new ArrayList<EventListener>();
            listenersMap.put( c, list );
        }
        list.add( l );
    }

}
