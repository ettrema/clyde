package com.bradmcevoy.web.children;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.web.BaseResourceList;
import com.bradmcevoy.web.CommonTemplated;

/**
 * TODO
 *
 * @author brad
 */
public class ResourceQueryService {
    public BaseResourceList list(CommonTemplated ct, Path path) {
        BaseResourceList list = new BaseResourceList();
        _list(ct, list,path,0);
        return list;
    }

    private void _list(CommonTemplated from, BaseResourceList list, Path path, int index) {
        String part = path.getParts()[index];
        if( part.equals( "*")) {

        }
        from.find( part );
    }

    private class Part {
        private String fileName;

    }
}
