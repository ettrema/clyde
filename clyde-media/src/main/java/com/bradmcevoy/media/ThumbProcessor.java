package com.bradmcevoy.media;

import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.Thumb;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


/**
 *
 * @author brad
 */
public interface ThumbProcessor {

    public enum Rotate {
        NONE,
        LEFT,
        RIGHT
    }



    public int generateThumbs( ImageFile imageFile, List<Thumb> thumbs, boolean skipIfExists ) throws FileNotFoundException, IOException;

    public int generateThumbs( ImageFile imageFile, List<Thumb> thumbs, Rotate rotate, boolean skipIfExists ) throws FileNotFoundException, IOException;

    //private void createThumb( ImageFile imageFile, Folder folder, int width, int height, boolean skipIfExists, BufferedImage image );


}
