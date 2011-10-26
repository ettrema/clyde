package com.bradmcevoy.web.creation;

import com.bradmcevoy.web.*;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.component.HtmlInput;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class DefaultResourceCreator implements ResourceCreator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Folder.class);

    @Override
    public BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        log.debug("defaultCreateItem: " + ct);
        BaseResource res;
        if (ct.contains("html")) {
            Page page = new Page(folder, newName);
            HtmlInput root = new HtmlInput(page, "root");
            if( in != null ) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                StreamUtils.readTo(in, bout);
                root.setValue(bout.toString());
                page.getComponents().add(root);
            }
            page.save();
            res = page;
        } else if( ct.contains("folder")) {
            Folder f = new Folder(folder,newName);
            f.save();
            res = f;
//        } else if (ct.contains("image")) {
//            ImageFile image = new ImageFile(ct, folder, newName);
//            image.save();
//            if( in != null ) image.setContent(in);
//            res = image;
        } else if (ct.contains("text")) {
            TextFile tf = new TextFile(ct, folder, newName);
            tf.save();
            if( in != null ) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                StreamUtils.readTo(in, bout);
                tf.setContent(bout.toString());
                tf.save();
            }
            res = tf;
//        } else if( ct.contains("flash")) {
//            FlashFile image = new FlashFile(ct, folder, newName);
//            image.save();
//            if( in != null ) image.setContent(in);
//            res = image;
//        } else if( ct.contains("video")) {
//            VideoFile video = new VideoFile(ct, folder, newName);
//            video.save();
//            if( in != null ) video.setContent(in);
//            res = video;
        } else {
            BinaryFile image = new BinaryFile(ct, folder, newName);
            image.save();
            if( in != null ) image.setContent(in);
            res = image;
        }

        return res;
    }

    @Override
    public void addCreator( Creator creator ) {

    }

}
