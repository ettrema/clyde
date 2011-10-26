
package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.ettrema.utils.ClydeUtils;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.ComponentMap;
import com.bradmcevoy.web.Expression;
import com.bradmcevoy.web.File;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.Web;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;

public class CreateCommand extends Command {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CreateCommand.class);
    
    private static final long serialVersionUID = 1L;
    
    protected TemplateInput newName;
    protected Text templateName;
    protected Expression afterCreateScript;
    protected Expression nextPageScript;
    protected TemplateInput folder;
    protected Component captcha;
    private boolean autoName;
    
    public CreateCommand(Addressable container, String name) {
        super(container,name);
        newName = new TemplateInput(this, "newName");
        templateName = new Text(this, "templateName");
        afterCreateScript = new Expression(this, "afterCreateScript");
        folder = new TemplateInput(this, "folder");
        captcha = null;
    }

    public CreateCommand(Addressable container, Element el) {
        super(container, el);
    }

    @Override
    public void fromXml(Element el) {
        super.fromXml(el);
        fromLocalXml(el);
    }

    public void fromLocalXml(Element el) {
        this.autoName = InitUtils.getBoolean(el, "autoname");
        ComponentMap components = new ComponentMap();
        components._fromXml(this, el);
        fromXml(components);
    }



    protected void fromXml(ComponentMap map) {
        Component c = consume(map, "newName");
        if( c == null ) {
            newName = new TemplateInput(this, "newName");
        } else {
            newName = (TemplateInput) c;
        }
        c = consume(map, "templateName");
        if( c == null ) {
            templateName = new Text(this, "templateName");
        } else {
            templateName = (Text) c;
        }        
        c = consume(map, "afterCreateScript");
        if( c == null ) {
            afterCreateScript = new Expression(this, "afterCreateScript");
        } else {
            afterCreateScript = (Expression) c;
        }                
        c = consume(map, "nextPageScript");
        if( c == null ) {
            nextPageScript = new Expression(this, "nextPageScript");
        } else {
            nextPageScript = (Expression) c;
        }
        c = consume(map, "folder");
        if( c == null ) {
            folder = new TemplateInput(this, "folder");
        } else {
            folder = (TemplateInput) c;
        }
        captcha = consume(map, "captcha");
    }

    protected Component consume(ComponentMap map, String name) {
        Component c = map.get(name);
        return c;
    }
    
    public Text getNewName() {
        return newName;
    }

    public Text getTemplateName() {
        return templateName;
    }

    public Component getCaptcha() {
        return captcha;
    }
    

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        super.onPreProcess(rc, parameters, files);
        newName.onPreProcess(rc, parameters, files);
        templateName.onPreProcess(rc, parameters, files);
        afterCreateScript.onPreProcess(rc, parameters, files);
        if( nextPageScript != null ) {
            nextPageScript.onPreProcess( rc, parameters, files );
        }
        if( captcha != null ) {
            captcha.onPreProcess( rc, parameters, files );
        }
    }
    
    
    
    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug("onProcess");
        if( !isApplicable(parameters) ) {
            return null; // not this command
        }
        if(autoName) {
            Folder folderToCreateIn = folderToCreateIn( rc );
            setNameAutomagically(rc, folderToCreateIn);
        }
        if( !validate(rc) ) {
            log.debug("validation failed");
            return null;
        }
        log.debug("doing create...");
        String newHref = doProcess(rc,parameters,files);
        return newHref;
    }

    @Override
    public boolean validate(RenderContext rc) {
        boolean b = true;
        b = b & newName.validate(rc);
        b = b & validateName(rc);
        b = b & templateName.validate(rc);
        log.debug( "validate: " + captcha);
        if( captcha != null ) {
            captcha.validate( rc );
        }
        return b;
    }
    
    protected boolean validateName(RenderContext rc) {
        String n = getNameToCreate(rc);
        Folder folderToCreateIn = folderToCreateIn(rc);
        if( folderToCreateIn.child(n) != null ) {
            newName.setValidationMessage("That name is already taken, please choose another");
            log.debug("name validation failed");
            return false;
        } else {
            return true;
        }
    }

    protected boolean isApplicable(Map<String, String> parameters) {
        String s = parameters.get(this.getName());
        return ( s != null );

    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {                
        String nameToCreate = getNameToCreate(rc);
        log.debug("doing create : " + nameToCreate + " - " + this.getClass().getName());
        Folder folderToCreateIn = folderToCreateIn(rc);
        Web web = folderToCreateIn.getWeb();
        if( web == null ) throw new NullPointerException("No web for resource: " + folderToCreateIn.getPath());

        String sTemplate = templateName.getFormattedValue();
        log.debug("...template: " + sTemplate);
        if( sTemplate == null || sTemplate.trim().length() == 0 ) {
            throw new IllegalArgumentException("No template specified for new item.");
        }
        ITemplate t = web.getTemplate(templateName.getFormattedValue());
        if( t == null ) {
            throw new IllegalArgumentException("Template doesnt exist: " + templateName.getFormattedValue());
        }
        BaseResource res = t.createPageFromTemplate(folderToCreateIn, nameToCreate);
        res.save();
        log.debug("created a: " + res.getClass() + " called " + res.getName() + " at href: " + res.getHref());
        afterCreate(res,rc);
        res.save();
        commit();
        log.debug("done commit");
        String url = nextPage( res, rc );
        log.debug( "next page: " + url);
        return url;
    }

    protected Folder folderToCreateIn(RenderContext rc) {
        Templatable tr = rc.getTargetPage();
        if( folder != null ) {
            String sPath = folder.render(rc);
            Path path = Path.path(sPath);
            Templatable ctDest = tr.find(path); 
            if( ctDest == null ) {
                throw new RuntimeException( "Destination folder not found. From: " + tr.getHref() + " path: " + sPath);
            } else if( ctDest instanceof Folder ) {
                return (Folder) ctDest;                        
            } else {
                throw new RuntimeException("Desination folder is not a folder: " + ctDest.getHref() + " is a " + ctDest.getClass());
            }
        } else {
            if( tr instanceof File ) {
                File page = (File)tr;

                Folder folderToCreateIn = page.getParent();
                return folderToCreateIn;
            } else if( tr instanceof Folder ) {
                return (Folder) tr;
            } else {
                throw new RuntimeException("Render target page not valid type. Is a: " + tr.getClass() + " but must be a File or  Folder" );
            }
        }        
    }
    
    public String getNameToCreate(RenderContext rc) {
        String s = newName.render(rc);
        log.debug("new name: " + s);
        return s;
    }

    protected void afterCreate(BaseResource newlyCreated, RenderContext rc) {
        if( afterCreateScript == null) return;
        log.debug("running after create script");
        Map map = new HashMap();
        map.put("created", newlyCreated);
        map.put("rc", rc);
        map.put("command", this);
        Templatable ct = (Templatable) this.getContainer();
        afterCreateScript.calc(ct, map);
        log.debug("done aftercreate");
    }

    protected String nextPage(BaseResource newlyCreated, RenderContext rc) {
        if( nextPageScript == null) return newlyCreated.getHref();
        log.debug("running next page script");
        Map map = new HashMap();
        map.put("created", newlyCreated);
        map.put("rc", rc);
        map.put("command", this);
        Templatable ct = (Templatable) this.getContainer();
        Object o = nextPageScript.calc(ct, map);
        if( o == null ) return newlyCreated.getHref();
        if( o instanceof Templatable ) {
            Templatable t = (Templatable) o;
            return t.getHref();
        } else {
            return o.toString();
        }
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);

        populateLocalXml( e2);
    }

    public void populateLocalXml(Element e2) {
        newName.toXml(container, e2);
        templateName.toXml(container, e2);
        afterCreateScript.toXml(container, e2);
        folder.toXml(container, e2);
        if (captcha != null) {
            captcha.toXml(container, e2);
        }
        if (nextPageScript != null) {
            nextPageScript.toXml(container, e2);
        }
        InitUtils.setBoolean(e2, "autoname", autoName);
    }

    protected void setNameAutomagically( RenderContext rc, Folder folderToCreateIn ) {
        Date dt = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime( dt );
        String n = ClydeUtils.getDateAsNameUnique( folderToCreateIn );
        log.debug( "set name: " + n);
        this.newName.setValue( n );
    }

}
