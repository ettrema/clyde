package com.ettrema.web.console2;

import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.console.ConsoleCommandFactory;
import com.ettrema.console.ConsoleResourceFactory;


/**
 *
 * @author brad
 */
public abstract class AbstractFactory  implements ConsoleCommandFactory{

    protected ResourceFactory resourceFactory;
    protected final String description;
    protected final String[] commandNames;

    public AbstractFactory( String description, String[] commandNames ) {
        this.description = description;
        this.commandNames = commandNames;
    }



    @Override
    public String[] getCommandNames() {
        return commandNames;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setConsoleResourceFactory( ConsoleResourceFactory crf ) {
        this.resourceFactory = crf;
    }
}
