package com.ettrema.web;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public interface ReplaceableHtmlParser {
    /**
     * Wrap the given html content with markers
     *
     * @param html
     * @param name
     * @return
     */
    String addMarkers(String html, String name);


    /**
     * Given some html which has replaceable parts identified using the
     * syntax implemented in addMarkers, build a map identifying the names
     * of those parts and their values (not including markers)
     *
     * @param html
     * @param names
     * @return
     */
    Map<String,String> parse(String html, Collection<String> names);
}
