package com.bradmcevoy.web.creation;

import com.bradmcevoy.web.*;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import java.io.InputStream;


/**
 *
 * @author brad
 */
public interface ResourceCreator {

    /**
     * add another creator to the list. Called at initialisation
     * 
     * @param creator
     */
    void addCreator(Creator creator);

    BaseResource createResource(Folder folder, String ct, InputStream in, String newName) throws ReadingException, WritingException;

}
