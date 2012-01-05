
package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.context.Context;
import com.ettrema.context.RequestContext;
import java.util.List;

public abstract class AbstractSeries implements DataSeries{
    
    protected BaseResource container;
    
    protected String name;
    
    protected Path folderPath;
    
    protected String template;
    
    protected String xFieldName;
    
    protected String yFieldName;

    protected abstract Tuple createTuple(Templatable res);
    
    public String getName() {
        return name;
    }
    
    protected Context ctx() {
        return RequestContext.getCurrent();
    }
    
    public TupleList getSeries(Object from, Object to) {
        List<Templatable> list = findRows();
        TupleList tuples = new TupleList();
        if( list == null ) return tuples;
        for( Templatable res : list ) {
            Tuple tuple = createTuple(res);
            tuples.add(tuple);
        }
        return tuples;
    }    

    
    protected List<Templatable> findRows() {
        Folder folder = findFolder();
        return folder.children(template);
    }

    

    private Folder findFolder() {
        Templatable ct = ComponentUtils.find(container, folderPath);
        if( ct == null ) return null;
        if( ct instanceof Folder ) {
            return (Folder) ct;
        } else {
            throw new RuntimeException("path does not refer to a folder. " + folderPath + " refers to a " + ct.getClass());
        }
    }   
}
