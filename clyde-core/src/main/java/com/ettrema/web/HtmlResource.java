package com.ettrema.web;

/**
 *
 * @author brad
 */
public interface HtmlResource {
    String getHref();

    String getUrl();

    String getName();

    String link( String text );

    String getLink();
}
