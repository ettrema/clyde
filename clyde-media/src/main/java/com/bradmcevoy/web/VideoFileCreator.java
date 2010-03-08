package com.bradmcevoy.web;

import com.bradmcevoy.web.creation.*;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class VideoFileCreator implements Creator {

    @Override
    public boolean accepts(String ct) {
        return ct.contains("video");
    }

    @Override
    public BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        VideoFile video = new VideoFile(ct, folder, newName);
        video.save();
        if (in != null) {
            video.setContent(in);
        }
        return video;
    }
}
