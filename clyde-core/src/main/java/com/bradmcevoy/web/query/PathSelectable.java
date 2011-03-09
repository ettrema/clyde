package com.bradmcevoy.web.query;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ResourceQueryProcessor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class PathSelectable implements Selectable, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PathSelectable.class);
    private static final long serialVersionUID = 1L;
    private Path path;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public List<FieldSource> getRows(Folder from) {
        if (log.isTraceEnabled()) {
            log.trace("getRows: from: " + from.getHref() + " path: " + path);
        }
        final List<FieldSource> rows = new ArrayList<FieldSource>();
        ResourceQueryProcessor resourceQueryProcessor = new ResourceQueryProcessor();
        resourceQueryProcessor.find(path, from, new ResourceQueryProcessor.ResourceConsumer() {

            public void onResource(Resource res) {
//                log.trace("matched resource: " + res.getName());
                if (res instanceof CommonTemplated) {
                    CommonTemplated tres = (CommonTemplated) res;
                    rows.add(new CommonTemplatedFieldSource(tres));
                }

            }
        });


        return rows;
    }

    public class CommonTemplatedFieldSource implements FieldSource {

        private final CommonTemplated resource;

        public CommonTemplatedFieldSource(CommonTemplated tres) {
            this.resource = tres;
        }

        public Object get(String name) {
            return resource.getValues().get(name);
        }

        public CommonTemplated getResource() {
            return resource;
        }

        public Object getData() {
            return resource;
        }
    }
}
