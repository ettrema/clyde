package com.bradmcevoy.web.image;

import org.apache.sanselan.*;
import com.bradmcevoy.common.UnrecoverableException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author brad
 */
public class ImageService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ImageService.class );

    public ImageService() {
        ImageIO.setUseCache( false );
    }

    public ImageService( File cache ) {
        ImageIO.setUseCache( true );
        ImageIO.setCacheDirectory( cache );
    }

    public Dimensions getDimenions( InputStream in, String name ) {
        try {
            ImageInfo info = Sanselan.getImageInfo( in, name );
            info.getHeight();
            info.getWidth();
            return new Dimensions( info.getWidth(), info.getHeight() );
        } catch( ImageReadException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

//    public void getExifData(InputStream in, String name) {
//        try {
//            IImageMetadata meta = Sanselan.getMetadata( in, name );
//            if( meta instanceof JpegImageMetadata) {
//                JpegImageMetadata jpegMeta = (JpegImageMetadata) meta;
//                jpegMeta.getExif().
//            } else {
//
//            }
//        } catch( ImageReadException ex ) {
//            throw new RuntimeException( ex );
//        } catch( IOException ex ) {
//            throw new RuntimeException( ex );
//        }
//
//    }
    public Dimensions getImageDimensions( File in ) {
        try {
            Dimensions d = null;
            BufferedImage image = ImageIO.read( in );
            d = new Dimensions( image.getWidth(), image.getHeight() );
            return d;
        } catch( IOException ex ) {
            throw new UnrecoverableException( in.getAbsolutePath(), ex );
        }
    }

    /**
     * Return scaled image.
     * Pre-conditions: (source != null) && (width > 0) && (height > 0)
     *
     * @param source the image source
     * @param width the new image's width
     * @param height the new image's height
     * @return the new image scaled
     */
    public BufferedImage getScaleImage( BufferedImage source, int width, int height ) {
        //assert(source != null && width > 0 && height > 0);
        long t = System.currentTimeMillis();
        Image img = source.getScaledInstance( width, height, Image.SCALE_FAST );

        BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        Graphics2D biContext = null;
        try {
            biContext = bi.createGraphics();
            biContext.drawImage( img, 0, 0, null );
        } finally {
            biContext.dispose();
        }
//        biContext.setComposite( AlphaComposite.Src );
//        biContext.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
//        biContext.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
//        biContext.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );


//        BufferedImage image = new BufferedImage(width, height, source.getType());
//        image.createGraphics().drawImage(source, 0, 0, width, height, null);
        t = System.currentTimeMillis() - t;
        log.debug( "scaling time: " + t );
        return bi;
    }

    /**
     * Return scaled image.
     * Pre-conditions: (source != null) && (xscale > 0) && (yscale > 0)
     *
     * @param source the image source
     * @param xscale the percentage of the source image's width
     * @param yscale the percentage of the source image's height
     * @return the new image scaled
     */
    public BufferedImage getScaleImage( BufferedImage source, double xscale, double yscale ) {
        //assert(source != null && width > 0 && height > 0);
        System.out.println( "scaling: " + xscale + " - " + yscale );
        return getScaleImage( source, (int) ( source.getWidth() * xscale ), (int) ( source.getHeight() * yscale ) );
    }

    public String getFormat( String name ) {
        return name.substring( name.lastIndexOf( '.' ) + 1 );
    }

    public boolean scaleProportionallyWithMax( File input, File output, int maxHeight, int maxWidth ) throws IOException {
        String name = input.getName();
        String format = name.substring( name.lastIndexOf( '.' ) + 1 );
        FileInputStream fin = null;
        FileOutputStream fout = null;
        try {
            fin = new FileInputStream( input );
            return scaleProportionallyWithMax( fin, fout, maxHeight, maxWidth, format );
        } finally {
            try {
                if( fin != null ) fin.close();
                if( fout != null ) fout.close();
            } catch( IOException ex ) {
                throw new RuntimeException( ex );
            }
        }
    }

    public boolean scaleProportionallyWithMax( InputStream in, OutputStream out, int maxHeight, int maxWidth, String format ) throws IOException {
        format = format.toLowerCase();
        BufferedImage image = read( in );
        if( image == null ) return false;
        Proportion prop = new Proportion( image.getWidth(), image.getHeight(), maxWidth, maxHeight );
        // find which dimension is the larger given its proportions
        if( prop.scaleByHeight() ) {
            int width = (int) ( image.getWidth() / prop.heightProp );
            if( width > 0 ) {
                image = getScaleImage( image, width, maxHeight );
            }
        } else if( prop.scaleByWidth() ) {
            int height = (int) ( image.getHeight() / prop.widthProp );
            if( height > 0 ) {
                image = getScaleImage( image, maxWidth, height );
            }
        } else {
            // do nothing
        }
        write( image, out, format );

        return true;
    }

    public boolean scaleProportionallyFromHeight( File input, File output, int height ) throws IOException {
        BufferedImage image = ImageIO.read( input );
        return scaleProportionallyFromHeight( image, output, height );
    }

    private boolean scaleProportionallyFromHeight( BufferedImage image, File output, int height ) throws IOException {
        if( image == null ) return false;
        int width = image.getWidth() * height / image.getHeight();
        image = getScaleImage( image, width, height );
        write( image, output );
        return true;
    }

    private void write( BufferedImage image, File output ) {
        String name = output.getName();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream( output );
            String format = name.substring( name.lastIndexOf( '.' ) + 1 ).toLowerCase();
            write( image, fileOutputStream, format );
        } catch( IOException ex ) {
            System.out.println( "throwing exception" );
            throw new UnrecoverableException( ex );
        } finally {
            if( fileOutputStream != null ) {
                try {
                    fileOutputStream.close();
                } catch( IOException ex ) {
                    throw new RuntimeException( ex );
                }
            }
        }
    }

    private static void write( BufferedImage image, OutputStream out, String format ) {
        try {
            BufferedOutputStream buffOut = new BufferedOutputStream( out );
            ImageIO.write( image, format, buffOut );
        } catch( IOException ex ) {
            System.out.println( "throwing exception" );
            throw new UnrecoverableException( ex );
        }
    }

    private BufferedImage read( InputStream is ) throws FileNotFoundException, IOException {
//        Iterator readers = ImageIO.getImageReadersByFormatName("jpg");
//        ImageReader reader = (ImageReader)readers.next();
//        ImageInputStream iis = ImageIO.createImageInputStream(is);
//        reader.setInput( iis );
//        ImageReadParam param = reader.getDefaultReadParam();
//        param.setSourceSubsampling(9, 9, 0, 0);
//        BufferedImage bi = reader.read(0, param);
//        return bi;
        ImageInputStream iis = ImageIO.createImageInputStream( is );
        // BufferedInputStream buf = new BufferedInputStream(is);
        return ImageIO.read( iis );
    }

    private class Proportion {

        double heightProp;
        double widthProp;
        double origHeight;
        double origWidth;

        public Proportion( double width, double height, double maxWidth, double maxHeight ) {
            if( maxHeight > 0 ) heightProp = height / maxHeight;
            if( maxWidth > 0 ) widthProp = width / maxWidth;
            origHeight = height;
            origWidth = width;
        }

        public boolean scaleByHeight() {
            return ( heightProp > 1 && heightProp > widthProp );
        }

        public boolean scaleByWidth() {
            return ( widthProp > 1 && widthProp > heightProp );
        }
    }
}
