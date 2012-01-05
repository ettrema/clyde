package com.ettrema.pay;

import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.web.Folder;
import com.ettrema.web.Page;
import com.ettrema.web.component.InitUtils;
import java.math.BigDecimal;
import org.jdom.Element;

/**
 * Debits are created when fees are due. A debit does not imply that a payment has been
 * made. You must total the balances to find out.
 *
 * @author brad
 */
@BeanPropertyResource( "clyde" )
public class Debit extends Page {

    private static final long serialVersionUID = -4839091476658574653L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Debit.class );

    public static Debit create( Folder parentFolder, String name, BigDecimal amount, String amountCurrency, String productCode, String description ) {
        if( productCode == null )
            throw new IllegalArgumentException( "must have productCode" );
        if( amount == null )
            throw new IllegalArgumentException( "must have amount" );
        if( amountCurrency == null )
            throw new IllegalArgumentException( "must have amountCurrency" );
        if( name == null )
            throw new IllegalArgumentException( "must have name" );

        Debit c = new Debit( parentFolder, name, amount, amountCurrency, productCode, description );
        c.save();
        return c;
    }

    private BigDecimal amount;
    private String amountCurrency;
    private String productCode;
    private String description;

    private Debit( Folder parentFolder, String name, BigDecimal amount, String amountCurrency,  String productCode, String description ) {
        super( parentFolder, name );
        this.amount = amount;
        this.amountCurrency = amountCurrency;       
        this.productCode = productCode;
        this.description = description;
        log.debug( "debit created: description: " + description );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.set( e2, "amount", amount );
        InitUtils.set( e2, "amountCurrency", amountCurrency );
        InitUtils.set( e2, "productCode", productCode );
        InitUtils.set( e2, "description", description );
    }

    @Override
    public boolean is( String type ) {
        if( type.equalsIgnoreCase( "receipt" ) ) return true;
        return super.is( type );
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }


    public String getProductCode() {
        return productCode;
    }

    public String getDescription() {
        return description;
    }
}
