package com.ettrema.web.mail;

import com.ettrema.web.velocity.VelocityInterpreter;
import com.ettrema.mail.StandardMessage;
import org.apache.velocity.VelocityContext;

/** Uses Velocity to process messages. Modifies the html and text properties only
 *
 * Sets the provided dataObject into the velocity context, named dataObject
 *
 */
public class VelocityTemplater implements Templater{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VelocityTemplater.class);

    @Override
    public void doTemplating(StandardMessage msg, Object dataObject) {
        log.debug("doTemplating");
        String html = msg.getHtml();
        if (html != null) {
            html = doTemplating(dataObject, html);
            msg.setHtml(html);
        }
        String text = msg.getText();
        if (text != null) {
            text = doTemplating(dataObject, text);
            msg.setText(text);
        }
    }

    private String doTemplating(Object dataObject, String template) {
        VelocityContext vc = new VelocityContext();
        vc.put("dataObject", dataObject);
        String s2 = VelocityInterpreter.evalToString(template, vc);
        log.debug("templated to: " + s2);
        return s2;
    }


}
