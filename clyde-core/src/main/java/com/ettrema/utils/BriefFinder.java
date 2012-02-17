package com.ettrema.utils;

/**
 * Utility service to extract a "brief" from content. It will attempt to
 * find the first paragraph, but truncate it to fit
 *
 * @author brad
 */
public class BriefFinder {
    public static String findBrief(String content, int max) {
        int pos = content.indexOf("<p");
        if( pos >= 0) {
            pos+=3;
            int finish = content.indexOf("</p", pos);
            if( finish > pos  ) {
                if( finish-pos > max) {
                    finish = pos + max;
                }
                return content.substring(pos, finish);
            } else {
                // no closing p tag found, so just limit to max of content length or max
                finish = pos + max;
                if( finish > content.length() ) {
                    finish = content.length()-1;
                }
                return content.substring(pos, finish);
            }
        } else {
            // maybe we can find a piece of text before any markup?
            int markupStartsAt = content.indexOf("<");
            if( markupStartsAt > 0) {
                int finish = markupStartsAt > max ? max : markupStartsAt;
                return content.substring(0, finish);
            } else {
                // there's no para's and no unmarkedup text, so nothing we can do
                return  null;
            }
        }
    }
}
