
package com.ettrema.web.search;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.web.BaseResource;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Template;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.Command;
import com.ettrema.web.component.NumberInput;
import com.ettrema.web.component.Text;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.jdom.Element;

public class SearchCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchCommand.class);
    private static final long serialVersionUID = 1L;
        
    private Text query;
    private NumberInput pageNum;
    
    private Document[] hits;
    
    public SearchCommand(Addressable container, String name) {
        super(container,name);
        initQuery();
        initPageNum();
    }

    public SearchCommand(Addressable container, Element el) {
        super(container, el);
        Map<String,Component> map = new HashMap<>();
        for( Object o : el.getChildren() ) {
            Element elComp = (Element) o;
            Component c = (Component) XmlUtils2.restoreObject(elComp,this);
            map.put(c.getName(), c);
        }        

        query = (Text) map.get("query");
        if( query == null ) {
            initQuery();
        }
        
        pageNum = (NumberInput) map.get("pageNum");
        if( pageNum == null ) {
            initPageNum();
        }        
    }

    public int getPrevious() {
        int i = getPage();
        if( i <= 1 ) return 1;
        return i-1;
    }
    
    public int getNext() {
        int i = getPage();
        int max = getNumPages();
        if( i >= max ) return max;
        return i+1;
    }
    
    private void initQuery() {
        query = new Text(this, "query");
        query.setRequestScope(true);        
    }
    
    private void initPageNum() {
        pageNum = new NumberInput(this, "pageNum");
        pageNum.setRequestScope(true);
        pageNum.setType("hidden");
    }
    
    public List<Resource> getResults() {
        List<Resource> list = new ArrayList<>();
        if( hits == null ) return list;
        
        VfsSession session = RequestContext.getCurrent().get(VfsSession.class);
        int pg = getPage()-1;     
        log.debug("-- showing page: " + pg);
        int pageSize = getPageLength();
        for( int i=0; i<getPageLength(); i++ ) {
            int hitNum = pg*pageSize + i;
            if( hitNum >= hits.length ) break;
            Document doc = hits[hitNum];
            String sId = doc.get("id");
            if( sId == null || sId.length() == 0 ) {
                log.warn("No id for hit");
            } else {
                UUID id = UUID.fromString(sId);
                NameNode nn = session.get(id);
                if( nn == null ) {
                    log.debug("didnt find node with id: " + id);
                } else {
                    BaseResource res = (BaseResource) nn.getData(); // todo: check type
                    if( res == null ) {
                        log.debug("no data node associated with name node: " + id);
                    } else if( res instanceof Template || res instanceof Folder ) {
                        // ignore
                    } else {
                        list.add(res);
                    }
                }
            }
        }
        log.debug("results: " + list.size());
        return list;
    }
    
    /**
     * 
     * @return - 1 indexed page number
     */
    public int getPage() {
        if( pageNum == null ) {
            log.warn("no pagenum component");
            return 1;
        }
        Integer ii = pageNum.getValue();
        log.debug("  pagenum: " + ii);
        if( ii == null ) return 1;
        if( ii.intValue() == 0 ) return 1;
        return ii;
    }
    
    public int getNumPages() {
        if( hits == null ) return 0;
        int num = hits.length / getPageLength() + 1;
        return num;
    }
    
    /**
     * 
     * @return - an ordered 1-indexed list of page numbers to show
     */
    public List<Integer> getPagesToShow() {
        int max = getNumPages();
        if( max > 10 ) max = 10;
        List<Integer> list = new ArrayList<Integer>();
        for( int i=1; i<=max; i++ ) list.add(i);
        return list;
    }
    
    public int getPageLength() {
        return 20;
    }
    
    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        super.onPreProcess(rc, parameters, files);
        query.onPreProcess(rc, parameters, files);
        pageNum.onPreProcess(rc, parameters, files);
    }

    
    
    public Document[] getHits() {
        return hits;
    }



    public Text getQuery() {
        return query;
    }

    public NumberInput getPageNum() {
        return pageNum;
    }

    
    
    
    @Override
    public Element toXml(Addressable container,Element el) {
        Element e2 = super.toXml(container,el);
        if( query != null ) {
            query.toXml(container, e2);
        }
        if( pageNum != null ) {
            pageNum.toXml(container, e2);        
        }
        return e2;
    }
       
    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        String s = parameters.get(this.getName());
        if( s == null ) return null; // not this command

        if( validate(rc) ) {
            doProcess(rc,parameters, files);
            return null;
        } else {
            log.debug("validation failed");
            return null;
        }
    }

    @Override
    public boolean validate(RenderContext rc) {
        return true;
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        if( !(this.container instanceof CommonTemplated) ) {
            log.warn("Search component's container is not templatable. Need to be able to find host");
            return null;
        }
        
        if( query == null ) {
            log.warn("query component is null");
            return null;
        }
        String exp = query.getValue();
        
        if( exp == null || exp.trim().length() == 0 ) {
            log.debug("No query string");
            return null;
        }
        
        CommonTemplated parent = (CommonTemplated) this.container;
        Host host = parent.getHost();
        if( host == null ) {
            log.warn("Could not find host from container: " + parent.getHref());
            return null;
        }
        String hostName = host.getName();
        SearchManager sm = requestContext().get(SearchManager.class);
        
        try {
            hits = sm.search(hostName, exp);
            log.debug("done search: " + hits.length);
        } catch (CorruptIndexException | ParseException ex) {
            query.setValidationMessage("Couldnt execute query: " + ex.getMessage()); // TODO, delete index
        }
        return null;        
    }
    
}
