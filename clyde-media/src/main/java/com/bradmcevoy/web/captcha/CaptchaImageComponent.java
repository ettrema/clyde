package com.bradmcevoy.web.captcha;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.InitUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import javax.imageio.ImageIO;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CaptchaImageComponent implements GetableResource, Component{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CaptchaComponent.class );
    private static final long serialVersionUID = 1L;
    static final String PARAM_CHALLENGE = "captcha_challenge";
    static final String PARAM_RESPONSE = "captcha_response";
    private String name;
    private Addressable container;

    public CaptchaImageComponent( Addressable container, Element el ) {
        this.container = container;
        this.name = InitUtils.getValue( el, "name" );
    }

    public CaptchaImageComponent( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public void init( Addressable container ) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate( RenderContext rc ) {
        return true;
    }

    public String render( RenderContext rc ) {
        log.debug( "render" );
        String ch = (String) rc.getAttribute(CaptchaComponent.PARAM_CHALLENGE);
        String html = "<img src='" + href( ch ) + "' />";
        return html;
    }

    public String renderEdit( RenderContext rc ) {
        return "";
    }

    public String getName() {
        return name;
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;
    }

    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        InitUtils.setString( e2, "name", name );
        InitUtils.setString( e2, "class", this.getClass().getName() );
        return e2;

    }

    private String href( String challenge ) {
        log.debug( "container path: " + container.getPath());
        return container.getName() + "/" + name + "?" + PARAM_CHALLENGE + "=" + challenge;
    }

// -------------- Resource stuff ---------------------------
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String arg3 ) throws IOException, NotAuthorizedException {
        log.debug( "sendContent" );
        try {
            String challenge = params.get( CaptchaComponent.PARAM_CHALLENGE );
            if( challenge == null ) {
                log.warn( "no captcha challenge given in request");
                return ;
            }
            CaptchaService svc = RequestContext.getCurrent().get( CaptchaService.class );
            if( svc == null ) {
                throw new RuntimeException( "no captachservice is configured" );
            }
            String resp = svc.getResponse( challenge );
            java.awt.image.BufferedImage image = textOnImage( resp );
            BufferedOutputStream buffOut = new BufferedOutputStream( out );
            ImageIO.write( image, "jpeg", buffOut );

        } catch( Throwable e ) {
            log.error( "exception rendering content", e );
        }
        log.debug( "finished" );
    }

    public static BufferedImage textOnImage( String str ) {
        try {
            // set font
            Font font = new Font( "Courier New", Font.PLAIN, 12 );
            // calc/guess bounding of text
            FontRenderContext frc = new FontRenderContext( null, true, false );
            TextLayout layout = new TextLayout( str, font, frc );
            Rectangle2D bounds = layout.getBounds();
            // create image
            int width = (int) bounds.getWidth() + (int) bounds.getX() + 4;
            int height = (int) bounds.getHeight() + 2;
            BufferedImage img = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
            Graphics2D g = img.createGraphics();
            // set complete background to white
            g.setColor( Color.WHITE );
            g.fillRect( 0, 0, img.getWidth(), img.getHeight() );
            // set font and colors
            g.setFont( font );
            g.setBackground( Color.WHITE );
            g.setColor( Color.BLACK );
            // render text
            float posX = 1;
            float posY = Math.abs( (float) bounds.getY() );
            g.drawString( str, posX, posY );
            // return image
            return img;
        } catch( Exception ex ) {
            return null;
        }
    }

    public Long getMaxAgeSeconds( Auth arg0 ) {
        return null;
    }

    public String getContentType( String arg0 ) {
        return "image/jpeg";
    }

    public Long getContentLength() {
        return null;
    }

    public String getUniqueId() {
        return null;
    }

    public Object authenticate( String arg0, String arg1 ) {
        return arg0;
    }

    public boolean authorise( Request arg0, Method arg1, Auth arg2 ) {
        return true;
    }

    public String getRealm() {
        return null;
    }

    public Date getModifiedDate() {
        return new Date();
    }

    public String checkRedirect( Request arg0 ) {
        return null;
    }

    private String buildSvg( String resp ) {
        String s = "" +
            "<?xml version='1.0' standalone='no'?>" +
            "<svg width='50.0px' xmlns:xlink='http://www.w3.org/1999/xlink' height='30.0px'" +
            "    xmlns='http://www.w3.org/2000/svg' version='1.0'>" +
            "    <text x='5.0' y='5.0'>" + resp + "</text>" +
            "</svg>";
        return s;
    }
}
