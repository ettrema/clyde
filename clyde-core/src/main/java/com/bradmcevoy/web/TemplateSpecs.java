
package com.bradmcevoy.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.TemplateSpecs.TemplateSpec;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TemplateSpecs extends ArrayList<TemplateSpec> implements Serializable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TemplateSpecs.class);

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param thisFolder
     * @return - the list of templates defined against the web which contains thisFolder
     */
    public static List<Template> findApplicable(Folder thisFolder) {
        Web web = thisFolder.getWeb();
        if( web == null ) {
            log.warn("no web for: " + thisFolder.getPath());
            return null;
        }
        Folder templatesFolder = web.getTemplates();
        if( templatesFolder == null ) {
            log.warn("no templates for web: " + web.getPath());
            return null;
        }
        List<? extends Resource> list = thisFolder.getWeb().getTemplates().getChildren();
        List<Template> list2 = new ArrayList<Template>();
        list2.add( new RootTemplate(templatesFolder));
        for( Resource r : list ) {
            if( r instanceof Template ) {
                list2.add((Template)r);
            }
        }
        return list2;
    }    
    
    public static TemplateSpecs parse(String s) {
        return new TemplateSpecs(s);
    }
    
    public static TemplateSpec parseSpec(String s) {
        if( s == null || s.length() == 0 ) return null;
        if( s.startsWith("-")) {
            s = s.substring(1);
            return new DisallowTemplateSpec(s);
        } else if( s.startsWith("+")) {
            s = s.substring(1);
            return new AllowTemplateSpec(s);            
        } else {
            return new AllowTemplateSpec(s);
        }        
    }
    

    public TemplateSpecs() {        
    }
    
    public TemplateSpecs(String specs) {
        if( specs == null ) return ;
        String[] arr = specs.split(" ");
        for( String s : arr ) {
            TemplateSpec spec = parseSpec(s);
            if( spec != null) this.add(spec);
        }        
    }
            
    public TemplateSpec add(String s) {
        TemplateSpec spec = parseSpec(s);
        this.add(spec);
        return spec;
    }
    
    public List<Template> findAllowed(Folder thisFolder) {    
        List<Template> list = findApplicable(thisFolder);
        if( list == null ) return null;

        TemplateSpecs specsToUse = getSpecsToUse(thisFolder);
        if( specsToUse == null || specsToUse.isEmpty()) {
            return list;
        } else {
            return specsToUse.findAllowed(list);
        }
    }    

    public List<Template> findAllowedDirect(Folder thisFolder) {
        List<Template> list = findApplicable(thisFolder);
        if( list == null ) return null;
        return findAllowed(list);
    }
    
    public List<Template> findAllowed(List<Template> all) {
        List<Template> allowed = new ArrayList<Template>(all);
        Iterator<Template> it = allowed.iterator();
        while( it.hasNext() ) {
            if( !isAllowed(it.next())) {
                it.remove();
            }
        }
        return allowed;
    }
    
    public boolean isAllowed(Template t) {
        for( TemplateSpec spec : this ) {
            Boolean b = spec.allow(t);
            if( b != null ) return b;
        }
        return true;
    }
    
    public String format() {
        StringBuffer sb = new StringBuffer();
        for( TemplateSpec spec : this ) {
            if( spec != null ) {
                spec.append(sb);
                sb.append(" ");
            }
        }
        return sb.toString();
    }
    
    
    
    public static abstract class TemplateSpec  implements Serializable{
        private static final long serialVersionUID = 1L;
        final String pattern;

        /**
         * Determine whether this rule applies, and if so what the answer is
         * 
         * @param t
         * @return - True, False, or null means don't care
         */
        abstract Boolean allow(Template t);
        
        abstract void append(StringBuffer sb);
        
        public TemplateSpec(String pattern) {
            this.pattern = pattern;
        }        
    }
    
    public static class AllowTemplateSpec extends TemplateSpec implements Serializable {
        private static final long serialVersionUID = 1L;
        public AllowTemplateSpec(String pattern) {
            super(pattern);
        }        
        
        @Override
        public Boolean allow(Template t) {
            if( pattern.equals("*")) {
                return Boolean.TRUE;
            } else {
                if( pattern.equals(t.getName()) ) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }
        }

        @Override
        void append(StringBuffer sb) {
            sb.append("+").append(pattern);
        }                
    }
    
    public static class DisallowTemplateSpec extends TemplateSpec implements Serializable {
        private static final long serialVersionUID = 1L;
        public DisallowTemplateSpec(String pattern) {
            super(pattern);
        }
        
        @Override
        public Boolean allow(Template t) {
            if( pattern.equals("*")) {
                return Boolean.FALSE;
            } else {
                if( pattern.equals(t.getName()) ) {
                    return Boolean.FALSE;
                } else {
                    return null;
                }
            }
        }    
        
        @Override
        void append(StringBuffer sb) {
            sb.append("-").append(pattern);
        }                        
    }

    private TemplateSpecs getSpecsToUse(Folder thisFolder) {
        TemplateSpecs specs = thisFolder.templateSpecs;
        if( thisFolder instanceof Host ) {
            return specs;
        } else {
            if( specs == null || specs.isEmpty() ) {
                return getSpecsToUse(thisFolder.getParent());
            } else {
                return specs;
            }
        }
    }
}
