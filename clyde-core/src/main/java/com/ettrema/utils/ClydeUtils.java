package com.ettrema.utils;

import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import java.util.UUID;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.ettrema.vfs.VfsSession;
import java.util.Calendar;
import java.util.Date;



import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class ClydeUtils {

    public static String getDateAsName() {
        return getDateAsName(false);
    }

    public static String getDateAsName(boolean seconds) {
        Date dt = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        String name = cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.MONTH) + "_" + cal.get(Calendar.DAY_OF_MONTH) + "_" + cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE);
        if (seconds) {
            name += "_" + cal.get(Calendar.SECOND);
        }
        return name;
    }

    public static String getDateAsNameUnique(CollectionResource col) {
        String name = getDateAsName();
        return getUniqueName(col, name);
    }

    public static String getUniqueName(CollectionResource col, String name) {
        Resource r = col.child(name);
        boolean isFirst = true;
        while (r != null) {
            name = com.bradmcevoy.io.FileUtils.incrementFileName(name, isFirst);
            isFirst = false;
            r = col.child(name);
        }
        return name.trim();
    }

    public static String pad(int i) {
        if( i < 10 ) {
            return "000" + i;
        } else if( i < 100 ) {
            return "00" + i;
        } else if(i < 1000) {
            return "0" + i;
        } else {
            return i + "";
        }
    }
	
	public static BaseResource loadResource(UUID id) {
		VfsSession vfs = _(VfsSession.class);
		NameNode node = vfs.get(id);
		if( node == null ) {
			return null;
		} else {
			DataNode data = node.getData();
			if( data == null ) {
				return null;
			} else {
				if( data instanceof BaseResource ) {
					return (BaseResource) data;
				} else {
					throw new RuntimeException("Item is not a " + BaseResource.class + " is a: " + data.getClass());
				}
			}
		}
	}
}