
package com.bradmcevoy.pay.mock;

import com.bradmcevoy.pay.PaymentService;
import com.ettrema.context.Context;
import com.ettrema.context.Factory;
import com.ettrema.context.Registration;
import com.ettrema.context.RootContext;

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
