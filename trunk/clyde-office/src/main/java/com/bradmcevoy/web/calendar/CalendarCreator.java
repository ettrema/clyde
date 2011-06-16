package com.bradmcevoy.web.calendar;

import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.creation.Creator;
import com.bradmcevoy.web.security.CurrentUserService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.ettrema.context.RequestContext.*;

/**
 * Creates calendar events
 *
 */
public class CalendarCreator implements Creator {

    public boolean accepts(String contentType) {
        return contentType.contains("text/calendar");
    }

    public BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        CalendarEvent e = new CalendarEvent(folder, newName);
        
        IUser creator = _(CurrentUserService.class).getOnBehalfOf();
        if (creator instanceof User) {
            e.setCreator((User) creator);
        }
        
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamUtils.readTo(in, bout);
        FileUtils.close(bout);
        String data = bout.toString();
        e.setiCalData(data);
        e.save();
        return e;
    }
}
