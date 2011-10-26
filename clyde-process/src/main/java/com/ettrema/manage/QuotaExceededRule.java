package com.ettrema.manage;

import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.Rule;
import com.bradmcevoy.process.Token;
import com.ettrema.process.TokenValue;
import com.ettrema.web.Host;
import com.ettrema.web.component.InitUtils;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class QuotaExceededRule implements Rule {

    private static final long serialVersionUID = 6212279102548479791l;

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( QuotaExceededRule.class );

    /**
     * The threshhold percent for triggering this rule
     */
    private int triggerPercent;

    /**
     * means that this rule triggers when the actual value exceeds the triggerPercent
     */
    private boolean triggerAbove;

    public QuotaExceededRule(Element el) {
        triggerPercent = InitUtils.getInt( el, "triggerPercent");
        triggerAbove = InitUtils.getBoolean( el, "triggerAbove");
    }


    public void arm( ProcessContext context ) {

    }

    public void disarm( ProcessContext context ) {

    }

    public boolean eval( ProcessContext processContext ) {
        Context context = RequestContext.getCurrent();
        QuotaManager qm = context.get( QuotaManager.class );
        if( qm == null ) throw new RuntimeException( "no quotamanager configured");
        Token token = processContext.getToken();
        TokenValue tokenValue = (TokenValue) token;
        Host host = tokenValue.getParentHost();

        Integer perc = qm.getUsagePercentage( host );
        if( perc != null ) {
            if( triggerAbove ) {
                return  perc > triggerPercent;
            } else {
                return perc < triggerPercent;
            }
        } else {
            log.warn("getUsagePercentage returned null");
            return false;
        }
    }

    public void populateXml( Element elRule ) {
        InitUtils.set( elRule, "triggerPercent", triggerPercent);
        InitUtils.setBoolean( elRule, "triggerAbove", triggerAbove);
    }
}
