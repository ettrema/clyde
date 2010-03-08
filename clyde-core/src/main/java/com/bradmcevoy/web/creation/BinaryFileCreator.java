package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class BinaryFileCreator implements Creator {

    @Override
    public boolean accepts(String contentType) {
        return true;
    }

    @Override
    public BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        BinaryFile image = new BinaryFile(ct, folder, newName);
        image.save();
        if (in != null) {
            image.setContent(in);
        }
        return image;
    }
}
