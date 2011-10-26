package com.ettrema.web.image;

import com.imageresize4j.ImageResizeProcessor;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 * @author brad
 */
public class HiQualityImageService extends ImageService{
    public HiQualityImageService() {
        super();
    }

    public HiQualityImageService( File cache ) {
        super(cache);
    }

    @Override
    public BufferedImage getScaledInstance( BufferedImage source, int targetWidth, int targetHeight ) {
        return resizeIn2PhasesViaIR4J( source, targetWidth, targetHeight, ImageResizeProcessor.TYPE_NEAREST_NEIGHBOR, ImageResizeProcessor.TYPE_IDEAL_5 );
    }

    public static BufferedImage resizeIn2PhasesViaIR4J( BufferedImage source, int destWidth, int destHeight,
        int firstInterpolation,
        int secondInterpolation ) {
        if( source == null ) {
            throw new NullPointerException( "source image is NULL!" );
        }
        if( destWidth <= 0 && destHeight <= 0 )
            throw new IllegalArgumentException( "destination width & height are both <=0!" );
        //calculate scale factors
        float scaleX = (float) destWidth / source.getWidth();
        float scaleY = (float) destHeight / source.getHeight();
        //check if we really need 2-phase schema
        if( scaleX < SCALE_LIMIT && scaleY < SCALE_LIMIT ) {
            //calculate the most appropriate intermediate image size
            int sizeMultiplier = 2;
            //if scale factors are too small then we need a larger intermediate image
            if( scaleX < SCALE_LIMIT / 2 || scaleY < SCALE_LIMIT / 2 )
                sizeMultiplier = 4;
            //create the processor for the 1-st phase
            ImageResizeProcessor preProcessor = new ImageResizeProcessor( firstInterpolation );
            //generate an intermediate image
            BufferedImage intermediate = preProcessor.resize( source, destWidth * sizeMultiplier, destHeight * sizeMultiplier );
            //create the processor for the final phase
            ImageResizeProcessor postProcessor = new ImageResizeProcessor( secondInterpolation );
            //generate the final result
            return postProcessor.resize( intermediate, destWidth, destHeight );
        } else {
            //just simple resize with the specified interpolation
            ImageResizeProcessor processor = new ImageResizeProcessor( secondInterpolation );
            return processor.resize( source, destWidth, destHeight );
        }
    }
}
