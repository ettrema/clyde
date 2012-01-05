package com.ettrema.pay;

import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.web.Folder;
import com.ettrema.web.Page;
import com.ettrema.web.component.InitUtils;
import java.math.BigDecimal;
import org.jdom.Element;

/**
 * Credits are created on payment, but are used to confer access to some
 * service afterwards, typically when a ProcessDef scans a Token
 *
 * @author brad
 */
@BeanPropertyResource( "clyde" )
public class Credit extends Page {

    private static final long serialVersionUID = -4839091476658574653L;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Credit.class );

    public static Credit create( Folder parentFolder, String name, BigDecimal amount, String amountCurrency, String buyerEmail, String productCode, String description ) {
        if( productCode == null )
            throw new IllegalArgumentException( "must have productCode" );
        if( amount == null )
            throw new IllegalArgumentException( "must have amount" );
        if( amountCurrency == null )
            throw new IllegalArgumentException( "must have amountCurrency" );
        if( name == null )
            throw new IllegalArgumentException( "must have name" );

        Credit c = new Credit( parentFolder, name, amount, amountCurrency, buyerEmail, productCode, description );
        c.save();
        return c;
    }
    /**
     * true iff the credit implied by this receipt has been used
     */
    private boolean used;
    private BigDecimal amount;
    private String amountCurrency;
    private String buyerEmail;
    private String productCode;
    private String description;

    private Credit( Folder parentFolder, String name, BigDecimal amount, String amountCurrency, String buyerEmail, String productCode, String description ) {
        super( parentFolder, name );
        this.amount = amount;
        this.amountCurrency = amountCurrency;
        this.buyerEmail = buyerEmail;
        this.productCode = productCode;
        this.description = description;
        log.debug( "credit created: description: " + description );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.set( e2, "used", used );
        InitUtils.set( e2, "amount", amount );
        InitUtils.set( e2, "amountCurrency", amountCurrency );
        InitUtils.set( e2, "buyerEmail", buyerEmail );
        InitUtils.set( e2, "productCode", productCode );
        InitUtils.set( e2, "description", description );
    }

    @Override
    public boolean is( String type ) {
        if( type.equalsIgnoreCase( "receipt" ) ) return true;
        return super.is( type );
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed( boolean used ) {
        this.used = used;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getDescription() {
        return description;
    }
}
