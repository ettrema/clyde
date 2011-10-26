package com.ettrema.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ReplaceableHtmlParserImpl implements ReplaceableHtmlParser {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReplaceableHtmlParserImpl.class);
    public static final String MARKER = "MARKER:";
    public static final String START_MARKER = MARKER + ":START";
    public static final String END_MARKER = MARKER + ":END";

    @Override
    public String addMarkers(String html, String name) {
        String r = startIdentifier(name);
        r = r + html;
        r = r + endIdentifier(name);
        return r;
    }

    @Override
    public Map<String,String> parse(String html, Collection<String> names) {
        Map<String, String> map = new HashMap<String, String>();
        for( String name : names ) {
            String start = startIdentifier(name);
            String end = endIdentifier(name);

            int startPos = html.indexOf(start);
            if( startPos >= 0) {
                startPos += start.length();
                int endPos = html.indexOf(end, startPos);
                if( endPos >= 0 ) {
                    String content = html.substring(startPos, endPos);
                    map.put(name, content);
                }
            }
        }
        if( names.contains( "title") && !map.containsKey( "title")) {
            log.debug( "should have title, but don't have title, so look for title");
            int start = html.indexOf( "<title>");
            if( start > 0 ) start = start + "<title>".length();
            int finish = html.indexOf( "</title>");
            if( start > 0 && finish > start ) {
                String title = html.substring( start, finish);
                map.put( "title", title);
            }
        }
        return map;
    }

    private String endIdentifier(String name) {
        return "<!-- " + MARKER + name + ":end -->";
    }

    private String startIdentifier(String name) {
        return "<!-- " + MARKER + name + ":start -->";
    }


}
