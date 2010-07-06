package com.bradmcevoy.web;

import com.bradmcevoy.web.component.ThumbsComponent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Thumb implements Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Thumb.class);
    private static final long serialVersionUID = 1L;

    public static List<Thumb> getThumbSpecs(Folder folder) {
        ThumbsComponent thumbs = (ThumbsComponent) folder.getComponent( "thumbSpecs" );
        if( thumbs == null ) {
//            log.debug("no thumbs component");
            return null;
        }
        List<Thumb> list = thumbs.getValue();
        if( list == null ) {
            return null;
        }
        return list;
    }

    String suffix;
    int height;
    int width;

    public static String format(List<Thumb> thumbs) {
        if( thumbs == null || thumbs.size() == 0 ) return "";
        StringBuffer sb = null;
        for( Thumb t : thumbs ) {
            if( sb == null ) {
                sb = new StringBuffer().append(t);
            } else {
                sb.append(",").append(t);
            }
        }
        return sb.toString();        
    }
    
    public static List<Thumb> parseThumbs(String sThumbs) {
        if (sThumbs == null || sThumbs.trim().length() == 0) {
            return null;
        }
        List<Thumb> list = new ArrayList<Thumb>();
        String[] arr = sThumbs.split(",");
        for (String s : arr) { // eg s=thumb:200:200 (width x height)
            Thumb t = parse(s);
            list.add(t);
        }
        return list;
    }

    public static Thumb parse(String spec) {
        String[] arrDim = spec.split(":");
        String suffix = "thumb";
        int width = 200;
        int height = 200;
        if (arrDim.length > 0) {
            suffix = arrDim[0];
        }
        if (arrDim.length > 1) {
            width = Integer.parseInt(arrDim[1]);
        }
        if (arrDim.length > 2) {
            height = Integer.parseInt(arrDim[2]);
        }
        Thumb t = new Thumb(suffix, width, height);
        return t;
    }

    public Thumb(String suffix, int width, int height) {
        super();
        this.suffix = suffix;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return suffix + ":" + width + ":" + height;
    }

    public String getSuffix() {
        return suffix;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }


}
