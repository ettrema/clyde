package com.ettrema.web;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Map;

/**
 *
 * @author bradm
 */
public interface ComponentEx extends Component {

    /**
     * Called after OnPreProcess for all components
     *
     * Commands should check to see if they have been invoked and if so execute
     *
     * This should perform the same task as onProcess, but returns richer
     * information
     */
    ProcessResult onProcessEx(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException;

    public class ProcessResult {

        private final String redirect;
        private final Object resultData;
        private final boolean executed;

        public ProcessResult(String redirect, Object resultData, boolean executed) {
            this.redirect = redirect;
            this.resultData = resultData;
            this.executed = executed;
        }

        /**
         * null if no redirect should occur, otherwise the url to redirect to
         *
         * @return
         */
        public String getRedirect() {
            return redirect;
        }

        /**
         * Any signficant data, should be JSON compatible
         *
         * @return
         */
        public Object getResultData() {
            return resultData;
        }

        /**
         * true iff the command executed
         *
         * @return
         */
        public boolean isExecuted() {
            return executed;
        }
    }
}
