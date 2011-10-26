package com.bradmcevoy.pay.paypal.sim;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author brad
 */
public class PaypalSimResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PaypalSimResourceFactory.class );
    String path;
    String itemName = "something or other";
    String itemNumber = "number 1";
    String paymentStatus = "Completed";
    String paymentCurrency = "USD";
    String txnId = "aTxnId";
    String receiverEmail = "paypal@bradmcevoy.com";
    String payerEmail = "buyer@email.com";
    PaypalSimResource theRes = new PaypalSimResource();

    public PaypalSimResourceFactory( String path ) {
        this.path = path;
    }

    public Resource getResource( String host, String path ) {
        if( this.path.equals( path ) ) {
            log.debug( "located paypal sim resource: " + path );
            return theRes;
        } else {
            return null;
        }
    }

    public String getSupportedLevels() {
        return "1";
    }

    public class PaypalSimResource implements GetableResource, PostableResource {

        /**
         *  true indicates this request is a verification callback from the server
         */
        boolean isCallBack;
        String error;

        public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
            log.debug( "sendContent" );
            PrintWriter pw = new PrintWriter( out );
            if( !isCallBack ) {
                pw.print( "<html>" );
                pw.print( "<body>" );
                pw.print( "<h1>I Am Paypal</h1>" );
                if( error != null ) {
                    pw.print( "<p>Error: " + error + "</p>" );
                }
                pw.print( "<form method='post' action='" + path + "'>" );
                pw.print( "<input type='hidden' name='notify_url' value='" + params.get( "notify_url" ) + "' />" );
                pw.print( "<input type='hidden' name='return' value='" + params.get( "return" ) + "' />" );
                pw.print( "amount <input type='text' name='amount' value='1'/>" );
                pw.print( "<input type='submit' value='doIt' name='doIt' />" );
                pw.print( "</form>" );
                pw.print( "</body>" );
                pw.print( "</html>" );
            } else {
                log.debug( "say VERIFIED" );
                pw.print( "VERIFIED" );
            }
            pw.flush();
            pw.close();
            out.flush();
        }

        public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) {
            log.debug( "processForm" );
            if( parameters.containsKey( "doIt" ) ) {
                isCallBack = false;
                error = null;
                try {
                    log.debug( "pretend to be IPN" );
                    String notifyUrl = parameters.get( "notify_url" );
                    String sAmount = parameters.get( "amount" );
                    BigDecimal amount = new BigDecimal( sAmount );
                    String returnUrl = parameters.get( "return" );
                    doIpnCall( notifyUrl, amount );
                    if( isCallBack ) {
                        return returnUrl; // todo: if ok response, then redirect back to site
                    } else {
                        error = "No callback";
                        return null;
                    }
                } catch( MalformedURLException ex ) {
                    throw new RuntimeException( ex );
                } catch( IOException ex ) {
                    throw new RuntimeException( ex );
                }
            } else if( parameters.containsKey( "cmd" ) ) {
                    log.debug( "got callback from server" );
                    isCallBack = true;
                    return null;
                } else {
                    log.debug( "form post, not not callback or form submit" );
                    isCallBack = false;
                    error = null;
                    return null;
                }

        }

        public Long getMaxAgeSeconds( Auth auth ) {
            return null;
        }

        public String getContentType( String accepts ) {
            return "text/html";
        }

        public Long getContentLength() {
            return null;
        }

        public String getUniqueId() {
            return null;
        }

        public String getName() {
            return "webscr";
        }

        public Object authenticate( String user, String password ) {
            return user;
        }

        public boolean authorise( Request request, Method method, Auth auth ) {
            return true;
        }

        public String getRealm() {
            return "paypal";
        }

        public Date getModifiedDate() {
            return null;
        }

        public String checkRedirect( Request request ) {
            return null;
        }

        private void doIpnCall( String notifyUrl, BigDecimal amount ) throws MalformedURLException, IOException {
            URL u = new URL( notifyUrl );
            URLConnection uc = u.openConnection();
            uc.setDoOutput( true );
            uc.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            PrintWriter pw = new PrintWriter( uc.getOutputStream() );
            String str = "";
            str += "item_name=" + URLEncoder.encode( itemName, "UTF-8" );
            str += "&item_number=" + URLEncoder.encode( itemNumber, "UTF-8" );
            str += "&payment_status=" + URLEncoder.encode( paymentStatus, "UTF-8" );
            str += "&mc_gross=" + URLEncoder.encode( amount.toPlainString() + "", "UTF-8" );
            str += "&mc_currency=" + URLEncoder.encode( paymentCurrency, "UTF-8" );
            str += "&txn_id=" + URLEncoder.encode( txnId, "UTF-8" );
            str += "&receiver_email=" + URLEncoder.encode( receiverEmail, "UTF-8" );
            str += "&payer_email=" + URLEncoder.encode( payerEmail, "UTF-8" );
            pw.println( str );
            pw.close();
            BufferedReader in = new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
            String res = in.readLine();
            in.close();
        }
    }
}
