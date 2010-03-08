package com.bradmcevoy.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MessageFolder;
import com.bradmcevoy.utils.StringUtils;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.Text;
import com.bradmcevoy.web.mail.MailProcessor;
import com.bradmcevoy.web.security.PermissionRecipient;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jdom.Element;

public class User extends Folder implements Mailbox, PermissionRecipient {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( User.class );
    private static final long serialVersionUID = 1L;
    public Text password;
    private List<String> groupNames;
    private boolean emailDisabled;

    public User( Folder parentFolder, String name ) {
        this( parentFolder, name, null );
        groupNames = new ArrayList<String>();
    }

    public User( Folder parentFolder, String name, String password ) {
        super( parentFolder, name );
        groupNames = new ArrayList<String>();
        this.password = new Text( this, "password" );
        this.password.setValue( password );
        this.componentMap.add( this.password );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        User uNew = (User) super.copyInstance( parent, newName );
        uNew.groupNames = new ArrayList<String>();
        uNew.groupNames.addAll( this.groupNames );
        return uNew;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new User( parent, newName );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        String sGroupNames = StringUtils.toString( groupNames );
        e2.setAttribute( "groupNames", sGroupNames );
        InitUtils.setBoolean( e2, "emailDisabled", emailDisabled );
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        password = (Text) this.componentMap.get( "password" );
        String s = el.getAttributeValue( "groupNames" );
        this.groupNames = StringUtils.fromString( s );
        this.emailDisabled = InitUtils.getBoolean( el, "emailDisabled" );
    }

    @Override
    public boolean isEmailDisabled() {
        return emailDisabled;
    }

    public void setEmailDisabled( boolean emailDisabled ) {
        this.emailDisabled = emailDisabled;
    }

    /**
     * returns true if this user is defined in an organisation which defines the given
     * web
     * 
     * @param web
     * @return
     */
    public boolean owns( Web web ) {
        Web webThisUser = this.getWeb();
        String thisWeb = webThisUser.getPath().toString();
        String thatWeb = web.getPath().toString();
        return thatWeb.contains( thisWeb ) && thatWeb.length() > thisWeb.length();
    }

    @Override
    public boolean authenticate( String password ) {
        if( this.password == null ) {
            return false;
        }
        String s = this.password.getValue();
        if( s == null ) {
            return password == null;
        } else {
            return s.equals( password );
        }
    }

    @Override
    public boolean authenticateMD5( byte[] passwordHash ) {
        try {
            if( this.password == null ) {
                return false;
            }
            String s = this.password.getValue();
            MessageDigest digest = java.security.MessageDigest.getInstance( "MD5" );
            byte[] actual = digest.digest( s.getBytes() );
            return java.util.Arrays.equals( actual, passwordHash );
        } catch( NoSuchAlgorithmException ex ) {
            throw new RuntimeException( ex );
        }
    }

    /**
     * 
     * @return - the email address for this user on this domain. NOT their specified
     *  external email
     */
    public Address getEmailAddress() {
        Host h = this.getHost();
        String s = this.getName() + "@" + h.getName();
        Address add;
        try {
            add = new InternetAddress( s );
        } catch( AddressException ex ) {
            throw new RuntimeException( ex );
        }
        return add;
    }

    @Override
    public MessageFolder getInbox() {
        log.debug( "getInbox" );
        Folder f = getEmailFolder();
        if( f == null ) {
            log.warn( "no inbox for: " + this.getName() );
            return null;
        }
        return new ClydeMessageFolder( f );
    }

    public Folder getEmailFolder() {
        return getEmailFolder( false );
    }

    public Folder getEmailFolder( boolean create ) {
        String emailFolderName = "inbox";
        Folder emailFolder = getMailFolder( emailFolderName, create );
        return emailFolder;
    }

    @Override
    public MessageFolder getMailFolder( String name ) {
        Folder f = getMailFolder( name, false );
        if( f == null ) {
            return null;
        }
        return new ClydeMessageFolder( f );
    }

    public Folder getMailFolder( String name, boolean create ) {
        String emailFolderName = "email_" + name;
        Folder emailFolder = getSubFolder( emailFolderName );
        if( emailFolder == null && create ) {
            try {
                emailFolder = (Folder) createCollection( emailFolderName, false );
            } catch( ConflictException ex ) {
                throw new RuntimeException( ex );
            }
        }
        return emailFolder;

    }

    @Override
    public void storeMail( MimeMessage mm ) {
        MailProcessor mailProc = requestContext().get( MailProcessor.class );
        if( mailProc == null )
            throw new RuntimeException( "No " + MailProcessor.class.getCanonicalName() + " is configured. Check catalog.xml" );
        String emailRecip = getExternalEmailText();
        if( emailRecip == null ) {
            Folder destFolder = getEmailFolder( true );
            mailProc.persistEmail( mm, destFolder, requestContext() );
            this.commit();
        } else {
            mailProc.forwardEmail( mm, emailRecip, requestContext() );
        }

    }

    boolean checkPassword( String password ) {
        String actualPassword = null;
        if( this.password != null ) {
            actualPassword = this.password.getValue();
        }
        if( actualPassword == null ) {
            return password == null || password.length() == 0;
        } else {
            return actualPassword.equals( password );
        }
    }

    boolean checkPassword( DigestResponse digestRequest ) {
        String actualPassword = null;
        if( this.password != null ) {
            actualPassword = this.password.getValue();
        }
        if( actualPassword == null ) {
            actualPassword = "";
        }

        DigestGenerator digestGenerator = new DigestGenerator();
        String serverDigest = digestGenerator.generateDigest( digestRequest, actualPassword );        
        boolean b = serverDigest.equals( digestRequest.getResponseDigest() );
        if( !b ) {
            log.warn("checkPassword failed: " + this.getName() + "/" + actualPassword);
        }
        return b;
    }

    /**
     * 
     * @return - the user's specified external email address as a string. Null if not specified
     */
    public String getExternalEmailText() {
        ComponentValue cvEmail = this.getValues().get( "email" );
        if( cvEmail == null || cvEmail.getValue() == null ) {
            return null;
        }
        String s = cvEmail.getValue().toString();
        if( s.trim().length() == 0 ) {
            return null;
        }
        return s;
    }

    public void setExternalEmailText( String email ) {
        ComponentValue cvEmail = this.getValues().get( "email" );
        if( cvEmail == null ) {
            cvEmail = new ComponentValue( "email", email );
            this.getValues().add( cvEmail );
        } else {
            cvEmail.setValue( email );
        }
    }

    @Override
    public boolean is( String type ) {
        if( isInGroup( type ) ) {
            return true;
        }
        return super.is( type );
    }

    public boolean isInGroup( String groupName ) {
        Resource r = this.getHost().getUsers().child( groupName );
        if( r instanceof Group ) {
            Group g = (Group) r;
            return isInGroup( g );
        } else {
            return false;
        }
    }

    public boolean isInGroup( Group g ) {
        if( getGroupNames() == null ) {
            log.debug( "has no groups: " + this.getName() );
            return false;
        }
        for( String name : getGroupNames() ) {
            if( name != null && name.equals( g.getName() ) ) {
                return true;
            }
        }
        return false;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames( List<String> names ) {
        this.groupNames = names;
    }

    public String getMobileNumber() {
        ComponentValue cvMobile = this.getValues().get( "mobile" );
        if( cvMobile == null || cvMobile.getValue() == null ) {
            return null;
        }
        String s = cvMobile.getValue().toString();
        if( s.trim().length() == 0 ) {
            return null;
        }
        return s;
    }

    /**
     * Required for PermissionRecipient. Just returns this.
     * 
     * @return - this
     */
    @Override
    public User getUser() {
        return this;
    }
}
