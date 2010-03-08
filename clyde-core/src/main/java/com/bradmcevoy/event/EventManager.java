package com.bradmcevoy.event;

/**
 *
 * @author brad
 */
public interface EventManager {
    void fireEvent(Event e);
    <T extends Event> void registerEventListener(EventListener l, Class<T> c);
}
