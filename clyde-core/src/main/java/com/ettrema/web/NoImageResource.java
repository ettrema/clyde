package com.bradmcevoy.web;

/**
 *
 * @author brad
 */
public class NoImageResource implements HtmlImage {

    @Override
    public String getHref() {
        return "/common/icons/" + getName();
    }

    @Override
    public String getImg() {
        return "<img src='" + getHref() + "' />";
    }

    @Override
    public String img( String onclick ) {
        return "<img onclick=\"" + onclick + "\" src='" + getHref() + "' />";
    }

    @Override
    public String getLinkImg() {
        String img = getImg();
        return "<a href='" + getHref() + "'>" + img + "</a>";
    }

    @Override
    public String getName() {
        return "no_image.gif";
    }

    @Override
    public String link( String text ) {
        return "<a href='" + getHref() + "'>" + text + "</a>";
    }

    @Override
    public String getLink() {
        String text = getName();
        return link( text );
    }

    @Override
    public String getUrl() {
        return getHref();
    }
}

