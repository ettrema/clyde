package com.ettrema.web.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author brad
 */
public class Scratch {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println( "hi" );
        ImageService imgSvc = new HiQualityImageService();
        //ImageService imgSvc = new ImageService();
        File fileIn = new File("c:\\test\\test.jpg");
        BufferedImage bufImg = ImageIO.read(fileIn);
        System.out.println( "scale" );
        long t = System.currentTimeMillis();
        BufferedImage scaled = imgSvc.getScaleImage( bufImg, 58, 78 );
        System.out.println( "scale time: " + (System.currentTimeMillis()-t) + "ms" );
        System.out.println( "done scale" );

        File fileOut = new File("c:\\test\\test-out.jpg");
        if( fileOut.exists()) {
            System.out.println( "deleting" );
            fileOut.delete();
        }
        FileOutputStream fout = new FileOutputStream( fileOut);

        System.out.println( "write output" );
        imgSvc.write( scaled, fout);

        fout.close();
    }
}
