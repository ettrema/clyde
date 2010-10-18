package com.bradmcevoy.web;

import com.bradmcevoy.web.wall.Wall;
import com.bradmcevoy.web.wall.WallItem;
import com.bradmcevoy.web.wall.WallService;
import java.util.Collections;
import java.util.List;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.mail.MessageFolder;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.Text;
import com.bradmcevoy.web.groups.GroupService;
import com.bradmcevoy.web.groups.RelationalGroupHelper;
import com.bradmcevoy.web.mail.MailProcessor;
import com.bradmcevoy.web.security.UserGroup;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

@BeanPropertyResource( "clyde" )
public class User extends Folder implements IUser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( User.class );
    private static final long serialVersionUID = 1L;
    public Text password;
    private boolean emailDisabled;

    public User( Folder parentFolder, String name ) {
        this( parentFolder, name, null );
    }

    public User( Folder parentFolder, String name, String password ) {
        super( parentFolder, name );
        this.password = new Text( this, "password" );
        this.password.setValue( password );
        this.componentMap.add( this.password );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        User uNew = (User) super.copyInstance( parent, newName );
        return uNew;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new User( parent, newName );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.setBoolean( e2, "emailDisabled", emailDisabled );

        Element elEmail = new Element( "email" );
        elEmail.setText( getExternalEmailText() );
        e2.addContent( elEmail );
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        password = (Text) this.componentMap.get( "password" );
        String s = el.getAttributeValue( "groupNames" );
        this.emailDisabled = InitUtils.getBoolean( el, "emailDisabled" );
        Element elEmail = el.getChild( "email" );
        if( elEmail != null ) {
            String newEmail = elEmail.getText();
            setExternalEmailText( newEmail );
        }
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
//    public boolean owns( Web web ) {
//        Web webThisUser = this.getWeb();
//        String thisWeb = webThisUser.getPath().toString();
//        String thatWeb = web.getPath().toString();
//        return thatWeb.contains( thisWeb ) && thatWeb.length() > thisWeb.length();
//    }
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
            } catch( NotAuthorizedException ex ) {
                throw new RuntimeException( ex );
            } catch( BadRequestException ex ) {
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

    /**
     * Pants way of stopping people from reading each others passwords...
     *
     * @param secretNumber - i'm not telling
     * @return
     */
    public String getPassword( int secretNumber ) {
        if( secretNumber == 847202 ) {
            if( this.password == null ) {
                return null;
            } else {
                return this.password.getValue();
            }
        } else {
            throw new RuntimeException( "Illegal secret number" );
        }
    }

    public void setPassword( String newPassword, int secretNumber ) {
        if( secretNumber == 847202 ) {
            if( this.password == null ) {
                this.password = new Text( this, "password" );
                this.componentMap.add( this.password );
            }
            this.password.setValue( newPassword );
        } else {
            throw new RuntimeException( "Illegal secret number" );
        }

    }

    boolean checkPassword( String password ) {
        String actualPassword = null;
        if( this.password != null ) {
            actualPassword = this.password.getValue();
        }
        if( actualPassword == null ) {
            boolean b = password == null || password.length() == 0;
            if( !b ) {
                log.info( "actual password is blank, but provided password is not" );
            }
            return b;
        } else {
            boolean b = actualPassword.equals( password );
            if( !b ) {
                log.info( "passwords don't match" );
            }
            return b;
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
            log.warn( "digest checkPassword failed: " + this.getName() + "/" + actualPassword );
        }
        return b;
    }

    /**
     * 
     * @return - the user's specified external email address as a string. Null if not specified
     */
    public String getExternalEmailText() {
        String s = getExternalEmailTextV2( "default" );
        if( s != null ) {
            return s;
        } else {
            ComponentValue cvEmail = this.getValues().get( "email" );
            if( cvEmail == null || cvEmail.getValue() == null ) {
                return null;
            }
            s = cvEmail.getValue().toString();
            if( s.trim().length() == 0 ) {
                return null;
            }
            return s;
        }
    }

    public void setExternalEmailText( String email ) {
        ComponentValue cvEmail = this.getValues().get( "email" );
        if( cvEmail == null ) {
            cvEmail = new ComponentValue( "email", this );
            cvEmail.init( this );
            cvEmail.setValue( email );
            this.getValues().add( cvEmail );
        } else {
            cvEmail.setValue( email );
        }
        setExternalEmailTextV2( "default", email );
    }

    @Override
    public boolean is( String type ) {
        if( isInGroup( type ) ) {
            return true;
        }
        return super.is( type );
    }

    public boolean isInGroup( String groupName ) {
        UserGroup group = _( GroupService.class ).getGroup( this, groupName );
        return group.isInGroup( this );
    }

    public boolean isInGroup( Group g ) {
        return g.isInGroup( this );
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

    public String getSubjectName() {
        return getName();
    }

    public void addToGroup( Group g ) {
        _( RelationalGroupHelper.class ).addToGroup( this, g );
    }

    public List<WallItem> getWall() {
        log.warn( "getWall");
        Wall wall = _(WallService.class).getUserWall( this,false );
        if( wall == null ) {
            return Collections.emptyList();
        } else {
            List<WallItem> list = wall.getItems();
            log.warn("wall items: " + list.size());
            return list;
        }
    }
}
