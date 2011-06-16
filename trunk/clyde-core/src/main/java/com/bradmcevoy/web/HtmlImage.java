package com.bradmcevoy.web;

/**
 * Represents something that can be used as an image in HTML.
 *
 * @author brad
 */
public interface HtmlImage extends HtmlResource {
    public String getImg();

    public String img(String onclick);

    public String getLinkImg();

   

}
