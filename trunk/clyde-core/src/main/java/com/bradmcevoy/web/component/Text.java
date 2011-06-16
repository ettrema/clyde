package com.bradmcevoy.web.component;

import com.bradmcevoy.web.User;
import com.bradmcevoy.utils.IntegerUtils;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.security.PasswordValidationService;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

public class Text extends AbstractInput<String> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Text.class );
    private static final long serialVersionUID = 1L;
    public Integer rows;
    public Integer cols;
    public Integer minLength;
    public Integer maxLength;

    public Text( Addressable container, String name ) {
        super( container, name );
        initValue();
    }

    public Text( Addressable container, String name, String value ) {
        this( container, name );
        setValue( value );
    }

    public Text( Addressable container, Element el ) {
        super( container, el );
    }

    protected void initValue() {
        setValue( "" );
    }

    @Override
    public boolean validate( RenderContext rc ) {
        log.debug( "validate: " + name );
        String s = getValue();
        if( s == null || s.trim().length() == 0 ) {
            if( required ) {
                setValidationMessage( "Required field" );
                return false;
            }
        } else {
            int l = s.trim().length();
            if( maxLength != null ) {
                if( l > maxLength ) {
                    setValidationMessage( "Is too long. Maximum length is " + maxLength );
                    return false;
                }
            }
            if( minLength != null && l < minLength ) {
                setValidationMessage( "Is too short. Minimum length is " + minLength );
                return false;
            }
        }

        // Yeah, this is pretty crap...
        if( this.name.equals( "password" ) ) {
            if( this.container instanceof User ) {
                User user = (User) this.container;
                String err = _( PasswordValidationService.class ).checkValidity( user, s );
                if( err != null ) {
                    setValidationMessage( err );
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void fromXml( Element el ) {
        super.fromXml( el );
        rows = IntegerUtils.parseInteger( el.getAttributeValue( "rows" ) );
        cols = IntegerUtils.parseInteger( el.getAttributeValue( "cols" ) );
        minLength = IntegerUtils.parseInteger( el.getAttributeValue( "minLength" ) );
        maxLength = IntegerUtils.parseInteger( el.getAttributeValue( "maxLength" ) );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element elThis = super.toXml( container, el );
        InitUtils.set( elThis, "rows", rows );
        InitUtils.set( elThis, "cols", cols );
        InitUtils.set( elThis, "minLength", minLength );
        InitUtils.set( elThis, "maxLength", maxLength );
        return elThis;
    }

    @Override
    protected String editTemplate() {
        String template;
        if( rows == null || rows <= 1 ) {
            if( this.name.equals( "password" ) ) { // nasty little hack, but alternative is data migration
                template = "<input type='password' name='${path}' value='${formattedValue}' size='${input.cols}' />";
            } else {
                template = "<input type='text' name='${path}' value='${formattedValue}' size='${input.cols}' />";
            }
        } else {
            template = "<textarea name='${path}' rows='${input.rows}' cols='${input.cols}'>${formattedValue}</textarea>";
        }
        return template;
    }

    @Override
    protected String parse( String s ) {
        return s;
    }

    public Integer getCols() {
        return cols;
    }

    public Integer getRows() {
        return rows;
    }

    public void setCols( Integer cols ) {
        this.cols = cols;
    }

    public void setRows( Integer rows ) {
        this.rows = rows;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }
}
