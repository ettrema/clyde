package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.component.HtmlInput;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class PageCreator implements Creator {

    @Override
    public boolean accepts(String contentType) {
        return contentType.contains("html");
    }

    @Override
    public BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        Page page = new Page(folder, newName);
        HtmlInput root = new HtmlInput(page, "root");
        if (in != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(in, bout);
            root.setValue(bout.toString());
            page.getComponents().add(root);
        }
        page.save();
        return page;
    }
}
