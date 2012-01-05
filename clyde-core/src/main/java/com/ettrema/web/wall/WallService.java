package com.ettrema.web.wall;

import com.ettrema.web.User;
import com.ettrema.web.Web;

/**
 * A WallService represents a means of accessing and manipulating user "Wall"s
 * 
 * A Wall is a chronological list of events, where the events have significance
 * but are otherwise unstructured.
 *
 * @author brad
 */
public interface WallService {

    Wall getUserWall( User user, boolean create ) ;

    Wall getWebWall( Web web, boolean create );

	void clearWall( Web web );

}
