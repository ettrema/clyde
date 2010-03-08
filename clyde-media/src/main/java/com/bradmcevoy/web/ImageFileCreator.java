package com.bradmcevoy.web;

import com.bradmcevoy.web.creation.*;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class ImageFileCreator implements Creator {

    @Override
    public boolean accepts(String ct) {
        return ct.contains("image");
    }

    @Override
    public BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        ImageFile image = new ImageFile(ct, folder, newName);
        image.save();
        if (in != null) {
            image.setContent(in);
        }
        return image;
    }
}
