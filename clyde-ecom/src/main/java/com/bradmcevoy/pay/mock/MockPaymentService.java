
package com.bradmcevoy.pay.mock;

import com.bradmcevoy.pay.Amount;
import com.bradmcevoy.pay.CardDetails;
import com.bradmcevoy.pay.PaymentService;
import com.bradmcevoy.pay.Result;
import com.bradmcevoy.pay.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockPaymentService implements PaymentService{

    public final List<Payment> payments = new ArrayList<Payment>();
    public final Map<String,CardDetails> customers = new HashMap<String, CardDetails>();

    public Result doPayment(Transaction tx, CardDetails details) {
        Payment p = new Payment(details, tx);
        payments.add(p);
        return new Result("OK", null, true, "A");
    }

    public Result doPayment(Transaction transaction, String billingIdToSet, CardDetails details) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Result rebill(Transaction transaction, String registeredBillingId, String cardVerificationNo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(CardDetails details, String billingId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void refund(String txRef, Amount amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    

    public class Payment {
        CardDetails card;
        Transaction tx;

        public Payment(CardDetails card, Transaction tx) {
            this.card = card;
            this.tx = tx;
        }
    }
}
