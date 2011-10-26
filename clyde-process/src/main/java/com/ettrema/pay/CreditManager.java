package com.ettrema.pay;

import com.bradmcevoy.http.Resource;
import com.ettrema.manage.QuotaManager;
import com.bradmcevoy.process.Token;
import com.ettrema.utils.ClydeUtils;
import com.ettrema.utils.CurrentDateService;
import com.ettrema.web.Folder;
import com.ettrema.web.Formatter;
import com.ettrema.web.Host;
import java.math.BigDecimal;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author HP
 */
public class CreditManager {
	
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CreditManager.class );
	
    public static final String RECEIPTS_FOLDER = "_receipts";	
	
	private String currency = "USD";
	
	private final QuotaManager quotaManager;
		
	private final CurrentDateService currentDateService;
	
	private BigDecimal perGigMonthCost;
	
	private long secondsInMonth = 60*60*24*7*4l;
	
	private String varNameLastDebit = "_last_storage_debit_date_ms";

	@Autowired
	public CreditManager(QuotaManager quotaManager, CurrentDateService currentDateService) {
		this.quotaManager = quotaManager;
		this.currentDateService = currentDateService;
	}
	
	
	
	public void createCredit(Host host, BigDecimal amount, String productCode, String description) {
		try {
			
			Folder folder = getReceiptsFolder(host, true);
			String newName = ClydeUtils.getDateAsNameUnique(folder);
			Credit credit = Credit.create(folder, newName, amount, currency, null, productCode, description);
			credit.save();
			log.info("Created credit: " + credit);
		} catch (Throwable e) {
			throw new RuntimeException("Exception creating credit", e);
		}		
	}
	
	public void createStorageDebit(Token token, Host host, String productCode, String description) {
		Long hw = quotaManager.getHighwater(token);
		if( hw == null || hw.longValue() == 0) {
			log.info("no storage cost");
			return ;
		}
		BigDecimal bdHighwaterGigs = new BigDecimal(hw);
		DateTime dtLastDebitDate = getLastStorageDebitDate(token);
		DateTime dtNewDebitDate = new DateTime(currentDateService.getNow());
		org.joda.time.Duration actualBillingDuration = new Duration(dtLastDebitDate, dtNewDebitDate);
		BigDecimal dbStandardBillingDuration = new BigDecimal(secondsInMonth);
		BigDecimal proportionOfBillingPeriod = new BigDecimal(actualBillingDuration.getStandardSeconds()).divide(dbStandardBillingDuration);
		BigDecimal amount = perGigMonthCost.multiply(bdHighwaterGigs).multiply(proportionOfBillingPeriod);
		log.info("calc storage cost: proportion of billing period: " + proportionOfBillingPeriod + " perGig/Month:" + perGigMonthCost + " = " + amount);
		description = description + " (" + formatUsage(hw, dtLastDebitDate, dtNewDebitDate) + ")";
		createDebit(host, amount, productCode, description);
		setLastStorageDebitDate(token, dtNewDebitDate);
	}
	
	public void createDebit(Host host, BigDecimal amount, String productCode, String description) {
		try {
			
			Folder folder = getReceiptsFolder(host, true);
			String newName = ClydeUtils.getDateAsNameUnique(folder);
			Debit debit = Debit.create(folder, newName, amount, currency, productCode, description);
			debit.save();
			log.info("Created debit: " + debit);
		} catch (Throwable e) {
			throw new RuntimeException("Exception creating credit", e);
		}		
	}	
	
	public BigDecimal calcBalance(Host host){		
		try {
			Folder receipts = getReceiptsFolder(host, true);
			BigDecimal balance = BigDecimal.ZERO;
			BigDecimal x;
			for (Resource res : receipts.getChildren()) {
				if (res instanceof Credit) {
					Credit c = (Credit)res;
					x = c.getAmount();
					log.trace(" add credit: " + x);
					balance.add(x);
				} else if (res instanceof Debit) {
					Debit c = (Debit)res;
					x = c.getAmount();
					log.trace(" subtract debit: " + x);
					x = x.negate();
					balance.add(x);
				}
			}
			log.info("calcBalance: " + host.getName() + " = " + balance.toPlainString());
			return balance;
		} catch (Exception e) {
			throw new RuntimeException("Exception creating balance for: " + host.getName(), e);
		}
	}
	

    public static Folder getReceiptsFolder( Host host, boolean autocreate ) {
        Folder f = getReceiptsFolder( host );
        if( autocreate ) {
            if( f == null ) {
                f = new Folder( host, RECEIPTS_FOLDER );
                f.save();
            }
        }
        return f;
    }
	

    public static Folder getReceiptsFolder( Host host ) {
        Resource r = host.child( "_receipts" );
        if( r == null ) {
            log.debug( "no receipts folder in host: " + host.getName() );
            return null;
        } else if( r instanceof Folder ) {
                Folder receipts = (Folder) r;
                return receipts;
            } else {
                log.debug( "receipts folder is not a folder! " + r.getName() + " - " + r.getClass() );
                return null;
            }
    }

	public BigDecimal getPerGigMonthCost() {
		return perGigMonthCost;
	}

	public void setPerGigMonthCost(BigDecimal perGigMonthCost) {
		this.perGigMonthCost = perGigMonthCost;
	}

	private DateTime getLastStorageDebitDate(Token token) {
		String s = (String) token.getVariables().get("varNameLastDebit");
		return Formatter.getInstance().getDateTime(s);
	}

	private void setLastStorageDebitDate(Token token, DateTime dtNewDebitDate) {		
		token.getVariables().put(varNameLastDebit, dtNewDebitDate.toString());
	}

	private String formatUsage(Long hw, DateTime dtLastDebitDate, DateTime dtNewDebitDate) {
		String dt1 = Formatter.getInstance().formatDate(dtLastDebitDate);
		String dt2 = Formatter.getInstance().formatDate(dtNewDebitDate);
		String s = hw + " Gigs " + dt1 + " - " + dt2;
		return s;
	}

	
	
}
