package com.bradmcevoy.utils;

import org.apache.log4j.Logger;

/**
 *
 * @author HP
 */
public class LogUtils {
    public static void trace(Logger log, Object ... args) {
        if( log.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for(Object o : args) {
                sb.append(o).append(", ");
            }
            log.trace(sb);
        }
    }
}
