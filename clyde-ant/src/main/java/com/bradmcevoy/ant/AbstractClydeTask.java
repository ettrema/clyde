package com.bradmcevoy.ant;

import com.bradmcevoy.config.CatalogConfigurator;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable2;
import com.bradmcevoy.context.FactoryCatalog;
import com.bradmcevoy.context.RootContext;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class AbstractClydeTask extends Task {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractClydeTask.class);
    
    protected String path;
    
    protected File catalogPath;
    
    private final FactoryCatalog catalog;
    private RootContext rootContext;
    
    abstract void execute(Context context) throws BuildException;
    
    public AbstractClydeTask() {
        catalog = new FactoryCatalog();
    }
    
    @Override
    public void execute() throws BuildException {
        log.debug("execute");        
        System.out.println("execute: " + this.getClass().getName());
        CatalogConfigurator configurator = new CatalogConfigurator();
        try {
            rootContext = configurator.load(catalog,catalogPath);
            if( rootContext == null ) throw new NullPointerException("rootContext didnt get created");
            rootContext.execute(new Executable2() {
                public void execute(Context context) {
                    log.debug("calling execute(context)");
                    AbstractClydeTask.this.execute(context);
                }
            });
        } finally {
            if( rootContext != null ) rootContext.shutdown();
        }
    }
            
    public String getPath() {
        return path;
    }
        
    public void setPath(String path) {
        log.debug("setting path: " + path);
        this.path = path;
    }

    public File getCatalogPath() {
        return catalogPath;
    }

    public void setCatalogPath(File catalogPath) {
        log.debug("setting catalog path");
        this.catalogPath = catalogPath;
    }    
}
