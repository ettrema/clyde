package com.bradmcevoy.web.csv;

import au.com.bytecode.opencsv.CSVReader;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.query.Field;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author brad
 */
public class ViewUpdateHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ViewUpdateHelper.class);
    private final static ViewUpdateHelper theInstance = new ViewUpdateHelper();

    public static ViewUpdateHelper getInstance() {
        return theInstance;
    }

    public void fromCsv(InputStream in, Folder fromFolder, Select rootSelect) throws IOException {
        InputStreamReader r = new InputStreamReader(in);
        CSVReader reader = new CSVReader(r);

        String[] lineParts;
        int line = 0;
        while ((lineParts = reader.readNext()) != null) {
            if (lineParts.length > 0) {
                line++;
                if(log.isTraceEnabled()) {
                    log.trace("process line: " + line + " : " + Arrays.toString(lineParts));
                }
                List<String> lineList = new ArrayList<String>();
                lineList.addAll(Arrays.asList(lineParts));
                if (lineList.size() > 0 && lineList.get(0).length() > 0) {
                    doProcess(rootSelect, lineList, fromFolder, line);
                }
            }
        }
        // TODO: find all recs not updated and delete them
    }

    private void doProcess(Select rootSelect, List<String> lineList, Folder folder, int line) {
        doProcess(rootSelect, folder, lineList, 0, line);
    }

    private void doProcess(Select select, Folder folder, List<String> lineList, int pos, int line) {
        log.trace("doProcess: " + pos);
        String name = lineList.get(pos++);
        if( name == null || name.length() == 0 ) {
            throw new RuntimeException("Cant save record with an empty name: column" + pos + " line: " + line);
        }
        Resource child = folder.child(name);
        if (child == null) {
            log.trace("Create child called: " + name);
            Folder parentfolder = folder;
            String templateToCreate = select.getType();
            log.trace("create new record with template: " + templateToCreate);
            ITemplate template = parentfolder.getTemplate(templateToCreate);
            if (template == null) {
                log.warn("can't create child: " + name + " because template can't be found: " + select.getType() + " line: " + line);
                return;
            } else {
                child = template.createPageFromTemplate(parentfolder, name);
                log.info("created new record: " + child.getName());
            }
        } else {
            log.trace("found record to update: " + child.getName());
        }
        updateRecord(child, select, lineList, pos, line);
    }

    private void updateRecord(Resource child, Select select, List<String> lineList, int pos, int line) {
        BaseResource childRes = (BaseResource) child;
        for (Field f : select.getFields()) {
            if (pos < lineList.size()) {
                String sVal = lineList.get(pos++);
                childRes.setValue(f.getName(), sVal);
            }
        }
        childRes.save();
        if (select.getSubSelect() != null) {
            if (child instanceof Folder) {
                Folder nextParentFolder = (Folder) child;
                doProcess(select.getSubSelect(), nextParentFolder, lineList, pos, line);
            } else {
                throw new RuntimeException("Not a folder: " + childRes.getHref() + " Is a: " + childRes.getClass() + " id: " + childRes.getNameNodeId() + " line: " + line);
            }

        }
    }
}
