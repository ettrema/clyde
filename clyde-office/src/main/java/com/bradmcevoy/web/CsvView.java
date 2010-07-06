
package com.bradmcevoy.web;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdom.Element;


public class CsvView extends com.bradmcevoy.web.File implements Replaceable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CsvView.class);
    
    private static final long serialVersionUID = 1L;
        
    public Path sourceFolder;
    
    /**
     * only include files which satisfy the is('') test for isType
     */
    private String isType;
    
    public CsvView(String contentType, Folder parentFolder, String newName) {
        super(contentType,parentFolder,newName);
    }

    public CsvView( Folder parentFolder, String newName) {
        this("text/csv",parentFolder,newName);
    }

    @Override
    protected String getHelpDescription() {
        return "Dynamically generates a CSV representaton of resources of a given type in a specified folder";
    }

    @Override
    protected void populateHelpAttributes( Map<String, String> mapOfAttributes ) {
        mapOfAttributes.put( "sourceFolder", "the path to the folder containing the resources to view. May be empty, which then uses the parent folder" );
        mapOfAttributes.put( "type", "the type of resource to output. Must satisfy the 'is' test. eg is(isType) must return true. May be null or empty, which will then include all resources (except the csvview itself)" );
    }





    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        InitUtils.setString(e2, "type", isType);
        InitUtils.set(e2, "sourceFolder", sourceFolder);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        isType = InitUtils.getValue(el, "type");
        String s = InitUtils.getValue(el, "sourceFolder", ".");
        sourceFolder = Path.path(s);        
    }

 
    
    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new CsvView(parent, newName);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        PrintWriter pw = new PrintWriter(out);
        CSVWriter writer = new CSVWriter(pw);
        Folder source;
        if( sourceFolder == null ) {
            source = this.getParent();
        } else {
            Templatable ct = this.getParent().find(sourceFolder);
            if( ct == null ) {
                throw new RuntimeException("Couldnt find sourceFolder: " + this.sourceFolder);
            }
            if( ct instanceof Folder ) {
                source = (Folder)ct;
            } else {
                throw new RuntimeException("sourceFolder does not refer to a folder. Is a: " + ct.getClass());
            }
        }
        for( Resource res : source.getChildren() ) {
            if( res instanceof CommonTemplated && (res != this) ) {
                CommonTemplated tres = (CommonTemplated) res;
                if( isType == null || isType.length() == 0 || tres.is(isType)) {
                    output(tres,writer);
                }
            }
        }
        pw.flush();
        pw.close();
    }


    private void output(CommonTemplated tres, CSVWriter writer) throws IOException {
        List<String> vals = new ArrayList<String>();
        ITemplate template = tres.getTemplate();
        if( template == null ) {
            log.warn("Couldnt find template for page: " + tres.getPath());
            return ;
        }
        
        vals.add(tres.getName());
        vals.add(template.getName());
        for( ComponentDef def : template.getComponentDefs().values() ) {
            String s = getTextualValue(def, tres);
            if( s == null ) {
                vals.add("");
            } else {
                vals.add(s);
            }
        }
        String[] arr = new String[vals.size()];
        vals.toArray(arr);
        writer.writeNext(arr);
    }
    
    private String getTextualValue(ComponentDef def, CommonTemplated tres) {
        ComponentValue val = tres.getValues().get(def.getName());
        if( val == null ) return null;
        Object o = val.getValue();
        if( o == null ) return null;
        String s = def.formatValue(o);
        if( s == null ) return null;
        return s;
    }
    
    public void replaceContent(InputStream in, Long length) {
        try {
            InputStreamReader r = new InputStreamReader(in);
            CSVReader reader = new CSVReader(r);
            List<BaseResource> processed = new ArrayList<BaseResource>();
            String [] line;
            while ((line = reader.readNext()) != null) {
                if( line.length > 0 ) {
                    List<String> lineList = new ArrayList<String>();
                    for( String s : line ) {
                        lineList.add(s);
                    }
                    BaseResource res = doProcess(lineList);
                    processed.add(res);
                }
            }
            List<? extends Resource> existing;
            if( this.isType != null && this.isType.length()>0 ) {
                existing = this.getParent().getChildren(this.isType);
            } else {
                existing = this.getParent().getChildren();
            }
            for( BaseResource res : processed ) {
                existing.remove(res);
            }
            existing.remove(this);
            List<? extends Resource> toDelete = existing;
            for( Resource resToDelete : toDelete ) {
                log.debug("..will delete: " + resToDelete.getName());
                log.warn("deleting disabled");
            }
            this.commit();
        } catch (Exception ex) {
            this.rollback();
            throw new RuntimeException(ex);
        }        
    }
    
    /**
     * Use the given tokenised line of values to locate, create and/or update
     * a resource
     * 
     * @param line
     */
    private BaseResource doProcess(List<String> line) {
        String name = line.get(0);
        String templateName;
        if( line.size() > 1 ) {
            templateName = line.get(1);
            line.remove(0);            
        } else {
            templateName = "";
        }
        line.remove(0);
        return doProcess(name,templateName,line);
    }

    private Folder getSourceFolder() {
        return (Folder) this.getParent().find(sourceFolder);
    }
    
    private BaseResource doProcess(String name, String templateName, List<String> line) {
        Folder fSource = getSourceFolder();
        BaseResource tres = (BaseResource) fSource.child(name);
        // todo: implement changing template
        if( tres == null ) {
//            log.debug("..creating a: " + templateName + " called " + name);
            ITemplate newTemplate = this.getWeb().getTemplate(templateName);
            if( newTemplate == null ) {
                throw new RuntimeException("No template called: " + templateName + " could be found");
            } else {
                tres = newTemplate.createPageFromTemplate(fSource, name);
            }
            tres.save();
        }
        doUpdate(tres, line);
        return tres;
    }
    
    /**
     * For each component definition, grab a value from the line and update it
     * 
     * @param tres
     * @param line
     */
    private Templatable doUpdate(Templatable tres, List<String> line) {
//        log.debug("..doUpdate: " + tres.getName());
        int pos = 0;
        ITemplate template = tres.getTemplate();
        boolean isChanged = false;
        for( ComponentDef def : template.getComponentDefs().values() ) {
            ComponentValue val = tres.getValues().get(def.getName());
            if( val == null ) {
                val = def.createComponentValue(tres);
                tres.getValues().add(val);
            }
            Object oldVal = val.getValue();
            if( pos < line.size() ) {
                String sNewVal = line.get(pos++);
                Object newVal = def.parseValue(val, tres,sNewVal);
                if( !equal(oldVal,newVal) ) {
//                    log.debug( "setting val: " + def.getName() + " to " + newVal);
                    val.setValue(newVal);
                    def.changedValue(val);
                    isChanged = true;
                } else {
//                    log.debug("..NOT setting val: " + def.getName() + " to " + newVal);
                }
            } else {
                log.debug("no value for: " + def.getName());
            }
        }
        if( isChanged ) {
            log.debug("..saving: " + tres.getName());
            if( tres instanceof BaseResource ) {
                ((BaseResource)tres).save();
            }
        }
        return tres;
    }

    
    private boolean equal(Object oldVal, Object newVal) {
        if( oldVal == null ) {
            return (newVal==null);
        } else {
            if( newVal == null ) {
                return false;
            } else {
                return oldVal.equals(newVal);
            }
        }
                
    }

    @Override
    public boolean isIndexable() {
        return false;
    }


}
