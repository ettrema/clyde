package com.bradmcevoy.web.image;

import java.text.ParseException;
import org.apache.sanselan.*;
import com.bradmcevoy.common.UnrecoverableException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

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

    public BufferedImage rotateLeft( BufferedImage image) {
        BufferedImage target = new BufferedImage( image.getHeight(), image.getWidth(), image.getType() );
        Graphics2D graphics = target.createGraphics();
        graphics.rotate( -Math.PI / 2 );
        graphics.translate( -image.getWidth(), 0 );

        return target;
    }

    public BufferedImage rotateRight( BufferedImage image ) {
        BufferedImage target = new BufferedImage( image.getHeight(), image.getWidth(), image.getType() );
        Graphics2D graphics = target.createGraphics();
        graphics.rotate( Math.PI / 2 );
        graphics.translate( 0, -image.getHeight() );
        return target;
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

    public ExifData getExifData( InputStream in, String name ) {

        try {
            IImageMetadata meta = Sanselan.getMetadata( in, name );
            if( meta instanceof JpegImageMetadata ) {
                JpegImageMetadata jpegMeta = (JpegImageMetadata) meta;

                Double locLat = null;
                Double locLong = null;
                TiffImageMetadata exif = jpegMeta.getExif();
                if( exif != null ) {
                    TiffImageMetadata.GPSInfo info = exif.getGPS();
                    if( info != null ) {
                        locLat = info.getLatitudeAsDegreesNorth();
                        locLong = info.getLongitudeAsDegreesEast();
                    }
                }

                TiffField valDate = jpegMeta.findEXIFValue( TiffConstants.TIFF_TAG_DATE_TIME );
                if( valDate != null ) {
                    DateFormat dateFormat = new SimpleDateFormat( "y:M:d H:m:s" );
                    try {
                        Date takenDate = dateFormat.parse( valDate.getStringValue() );
                        return new ExifData( takenDate, locLat, locLong );
                    } catch( ParseException ex ) {
                        return null;
                    }
                } else {
                    log.warn( "date not found" );
                    return null;
                }
            } else {
                return null;
            }
        } catch( ImageReadException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }

    }

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
        BufferedImage image = read( in );
        return scaleProportionallyWithMax( image, out, maxHeight, maxWidth, format );
    }

    public boolean scaleProportionallyWithMax( BufferedImage image, OutputStream out, int maxHeight, int maxWidth, String format ) throws IOException {
        format = format.toLowerCase();
        if( image == null ) {
            log.warn( "null image!!" );
            return false;
        }
        Proportion prop = new Proportion( image.getWidth(), image.getHeight(), maxWidth, maxHeight );
        // find which dimension is the larger given its proportions
        if( prop.scaleByHeight() ) {
            log.trace( "scale by height" );
            int width = (int) ( image.getWidth() / prop.heightProp );
            if( width > 0 ) {
                image = getScaleImage( image, width, maxHeight );
            } else {
                throw new RuntimeException( "got non-positive width: " + width );
            }
        } else if( prop.scaleByWidth() ) {
            log.trace( "scale by width" );
            int height = (int) ( image.getHeight() / prop.widthProp );
            if( height > 0 ) {
                image = getScaleImage( image, maxWidth, height );
            } else {
                throw new RuntimeException( "got non-positive height: " + height );
            }
        } else {
            log.warn( "doing nothing" );
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
//            Sanselan.writeImage( image, out, ImageFormat.IMAGE_FORMAT_JPEG, null);
            BufferedOutputStream buffOut = new BufferedOutputStream( out );
            if( !ImageIO.write( image, format, buffOut ) ) {
                throw new RuntimeException( "No writer could be found for format: " + format );
            }
            buffOut.flush();
            out.flush();
//        } catch( ImageWriteException ex ) {
//            throw new UnrecoverableException( ex );
        } catch( IOException ex ) {
            throw new UnrecoverableException( ex );
        }
    }

    public BufferedImage read( InputStream is ) throws FileNotFoundException, IOException {
//        BufferedImage image;
//        try {
//            image = Sanselan.getBufferedImage( is );
//        } catch( ImageReadException ex ) {
//            throw new IOException( ex );
//        }
//        return image;


//        Iterator readers = ImageIO.getImageReadersByFormatName("jpg");
//        ImageReader reader = (ImageReader)readers.next();
//        ImageInputStream iis = ImageIO.createImageInputStream(is);
//        reader.setInput( iis );
//        ImageReadParam param = reader.getDefaultReadParam();
//        param.setSourceSubsampling(9, 9, 0, 0);
//        BufferedImage bi = reader.read(0, param);
//        return bi;
//
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
   
        IOUtils.copy( is, bout );
        //ImageInputStream iis = ImageIO.createImageInputStream( buf );
        byte[] arr = bout.toByteArray();
        ImageInputStream iis = ImageIO.createImageInputStream( new ByteArrayInputStream( arr ) );
        BufferedImage image = ImageIO.read( iis );
        if( image == null ) {
            log.debug( "No ImageReader supports the given image data: listing known formats. input size: " + arr.length );
            for( String s : ImageIO.getReaderFormatNames() ) {
                log.debug( " - " + s );
            }
        }
        return image;
    }

    public class ExifData {

        private final Date date;
        private final Double locLat;
        private final Double locLong;

        public ExifData( Date date, Double locLat, Double locLong ) {
            this.date = date;
            this.locLat = locLat;
            this.locLong = locLong;
        }

        public Date getDate() {
            return date;
        }

        public Double getLocLat() {
            return locLat;
        }

        public Double getLocLong() {
            return locLong;
        }

        @Override
        public String toString() {
            return "Exif: date: " + date + " lat:" + locLat + " long:" + locLong;
        }
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
