package com.ettrema.web.mail;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MessageHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MailProcessorImpl.class);

    public static String formatHtml(String html, List<? extends Resource> files) {
        html = stripLeadingBody(html);
        Set<String> cids = parseContentIds(html);
        Map<String,EmailAttachment> attachmentsByCid = getAttachmentsMap(files);
        for( String cid : cids ) {
            log.debug("looking for cid: " +  cid);
            EmailAttachment att = attachmentsByCid.get(cid);
            if( att != null ) {
                log.debug("found: " + att.getName());
                html = html.replace(cid, att.getHref());
            } else {
                log.debug("cid not found");
            }
        }
        return html;
    }

    /**
     * Strips everything up to the leading body tag
     *
     * @param html
     * @return
     */
    public static String stripLeadingBody(String html) {
        String s = "<BODY";
        int pos = html.indexOf("<BODY");
        if( pos > 0 ) html = html.substring(pos+s.length());
        pos = html.indexOf(">");
        if( pos > -1 ) html = html.substring(pos+1);
        return html;
    }


    /**
     * Find all the cid: tags in the given text
     *
     * @param s
     * @return
     */
    public static Set<String> parseContentIds(String s) {
        Set<String> set = new HashSet<String>();
        int pos = s.indexOf("cid:");
        while( pos >= 0 ) {
            int posEnd = s.indexOf("\"", pos+1);
            if( posEnd < 0 ) posEnd = s.indexOf("'", pos+1);
            if( posEnd > 0 ) {
                String cid = s.substring(pos, posEnd);
                set.add(cid);
            }
            pos = s.indexOf("cid:", pos+1);
        }
        return set;
    }

    /**
     * Put all EmailAttachments in the list into a map keyed by their contentid,
     * if they have a non-null contentid
     *
     * @param files
     * @return
     */
    public static Map<String, EmailAttachment> getAttachmentsMap(List<? extends Resource> files) {
        Map<String,EmailAttachment> map = new HashMap<String, EmailAttachment>();
        for( Resource r : files ) {
            if( r instanceof Templatable ) {
                Templatable ct = (Templatable) r;
                if( ct instanceof EmailAttachment ) {
                    EmailAttachment ea = (EmailAttachment) ct;
                    String cid = ea.getContentId();
                    if( cid != null ) map.put("cid:" + cid, ea);
                }
            }
        }
        return map;
    }

}
