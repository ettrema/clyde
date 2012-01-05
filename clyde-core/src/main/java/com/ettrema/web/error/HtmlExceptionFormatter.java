package com.ettrema.web.error;

/**
 *
 * @author brad
 */
public class HtmlExceptionFormatter {

    public String formatExceptionAsHtml( Throwable e ) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");
        sb.append("<head>");
        sb.append( "\n");
        sb.append("<title>Error report</title>");
        sb.append( "\n");
        sb.append("</head>");
        sb.append("<body>");
        sb.append( "\n");
        sb.append( "<h1>" ).append( "Error report " ).append( "</h1>" );
        sb.append( "\n");
        buildHtml( e, sb );
        sb.append( "\n");
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    private void buildHtml( Throwable e, StringBuffer sb ) {
        sb.append( "<h2>" + e.toString() + "</h2>");
        sb.append( "\n");
        sb.append( "<ul>");
        sb.append( "\n");
        for( StackTraceElement el : e.getStackTrace()) {
            sb.append( "<li>" + el.getClassName() + "::" + el.getMethodName() + " (" + el.getLineNumber() + ")");
            sb.append( "\n");
        }
        sb.append( "</ul>");
        sb.append( "\n");
        if(e.getCause() != null ) {
            buildHtml( e.getCause(), sb);
        }
    }
}
