package com.bradmcevoy;

import com.bradmcevoy.config.CatalogConfigurator;
import com.bradmcevoy.context.Executable;
import com.bradmcevoy.context.FactoryCatalog;
import com.bradmcevoy.context.RootContext;
import java.io.File;

public class TestUtils {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestUtils.class);
    private static final FactoryCatalog catalog = new FactoryCatalog();
    private static RootContext rootContext;
    
    public static void setup() {  
        log.debug("setup");
        File file = new File("C:\\Work\\ettrema\\Clyde\\test\\com\\bradmcevoy\\catalog.xml");
        CatalogConfigurator configurator = new CatalogConfigurator();
        rootContext = configurator.load(catalog,file);
        if( rootContext == null ) throw new NullPointerException("rootContext didnt get created");
    }
    
    
    public static void runTest(Executable exec) {
        rootContext.execute( exec );
    }
    
    public static void tearDown() {
        log.debug("tearDown");
        rootContext.shutdown();
    }
}
