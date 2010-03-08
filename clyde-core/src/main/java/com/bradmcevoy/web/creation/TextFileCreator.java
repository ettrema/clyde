package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.TextFile;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class TextFileCreator implements Creator {

    @Override
    public boolean accepts(String ct) {
        return ct.contains("text");
    }

    @Override
    public BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        TextFile tf = new TextFile(ct, folder, newName);
        tf.save();
        if (in != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(in, bout);
            tf.setContent(bout.toString());
            tf.save();
        }
        return tf;
    }
}
