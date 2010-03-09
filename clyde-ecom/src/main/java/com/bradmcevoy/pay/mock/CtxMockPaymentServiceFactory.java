
package com.bradmcevoy.pay.mock;

import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Factory;
import com.bradmcevoy.context.Registration;
import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.pay.PaymentService;

public class CtxMockPaymentServiceFactory implements Factory<PaymentService>{
    public static Class[] classes = {PaymentService.class};
    
    public CtxMockPaymentServiceFactory() {
    }

    public Class[] keyClasses() {
        return classes;
    }

    public String[] keyIds() {
        return null;
    }

    public Registration<PaymentService> insert(RootContext context, Context requestContext) {
        PaymentService svc = new MockPaymentService();
        Registration<PaymentService> reg = requestContext.put(svc,this);
        return reg;
    }

    public void init(RootContext context) {
    }

    public void destroy() {
    }

    public void onRemove(PaymentService item) {
    }

}
