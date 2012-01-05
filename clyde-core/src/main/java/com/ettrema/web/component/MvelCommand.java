package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;



/**
 *
 * @author brad
 */
public class MvelCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MvelCommand.class);
    private static final long serialVersionUID = 1L;

    private String script;

    public MvelCommand(Addressable container, Element el) {
        super(container, el);
    }

    public MvelCommand(Addressable container, String name) {
        super(container, name);
    }

    @Override
    public void fromXml( Element el ) {
        super.fromXml( el );
        script = InitUtils.getValue( el );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        e2.setContent( new org.jdom.Text( script));
    }





    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        // TODO: validate
        if (!isApplicable(parameters)) {
            return null; // not this command
        } else {
            return doProcess(rc, parameters, files);
        }
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug("doProcess");
        String nextUrl = doProcess(rc, parameters);
        log.debug( "done script. next url: " + nextUrl);
        return nextUrl;
    }

    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    protected boolean isApplicable(Map<String, String> parameters) {
        String s = parameters.get(this.getName());
        return (s != null);
    }

    protected String doProcess(RenderContext rc, Map<String, String> parameters) {
        log.debug( "run script: " + script);
        RenderContext rcTarget = rc.getTarget();
        Map map = new HashMap();
        map.put( "targetPage", rcTarget.page);
        map.put( "params", parameters);
        map.put("rc", rc);
        map.put("request", RequestParams.current());
        map.put( "services", new Services());
        Object o = org.mvel.MVEL.eval(script, map);
        commit();
        if( o == null) {
            return null;
        } else if( o instanceof String) {
            String url = (String) o;
            return url;
        } else if( o instanceof CommonTemplated ) {
            CommonTemplated ct = (CommonTemplated) o;
            return ct.getHref();
        } else {
            log.warn( "unhandled return type: " + o.getClass());
            return rcTarget.page.getHref();
        }
    }

}
