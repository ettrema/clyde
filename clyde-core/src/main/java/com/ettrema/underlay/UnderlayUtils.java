package com.ettrema.underlay;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.ExistingResourceFactory;
import com.ettrema.web.Host;
import com.ettrema.web.Web;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class UnderlayUtils {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UnderlayUtils.class);
    
    /**
     * Walks over the underlays, in order, until one indicates a result has been
     * found by returning a non-null value
     *
     * @param target
     * @param visitor
     */
    public static <T> T walkUnderlays(Host target, UnderlayLocator underlayLocator, UnderlayVisitor<T> visitor) throws NotAuthorizedException, BadRequestException {
        List<UnderlayVector> vectors = target.getUnderlayVectors();
        if (vectors != null) {
            // do shallow search
            List<Host> hosts = new ArrayList<>(); // cache lookups so can re-use in deep search
            for (UnderlayVector v : vectors) {
                Host u = underlayLocator.find(v);
                hosts.add(u);
                if (u != null) {
                    T o = visitor.visitUnderlay(u);
                    if (o != null) {
                        return o;
                    }
                } else {
                    log.warn("Could not find underlay: " + v);
                }
            }
            // Now do deep search
            for (Host u : hosts) {
                T o = walkUnderlays(u, underlayLocator, visitor);
                if (o != null) {
                    return o;
                }
            }

        }
        return null;
    }

    public interface UnderlayVisitor<T> {

        /**
         * Return what you're looking for, if you find it. Otherwise return
         * null;
         *
         * @param underLayFolder
         * @return
         */
        T visitUnderlay(Web underLayFolder) throws NotAuthorizedException, BadRequestException;
    }
}
