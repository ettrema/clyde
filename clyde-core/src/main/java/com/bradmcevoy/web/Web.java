package com.bradmcevoy.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.web.component.ThemeSelect;

public class Web extends Folder {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Web.class );
    private static final long serialVersionUID = 1L;

    public static Web find( CommonTemplated t ) {
        if( t == null ) return null;
        if( t instanceof Web ) return (Web) t;
        return find( t.getParent() );
    }
    public static final String TRASH_FOLDER_NAME = "Trash";

    public Web( Folder parentFolder, String newName ) {
        super( parentFolder, newName );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Web( parent, newName );
    }

    @Override
    public Folder getTrashFolder() {
        Resource r = this.child( TRASH_FOLDER_NAME );
        if( r == null ) {
            try {
                r = this.createCollection( TRASH_FOLDER_NAME, false );
            } catch( ConflictException ex ) {
                throw new RuntimeException( "Cant create " + TRASH_FOLDER_NAME + " in " + this.getHref(), ex );
            }
            return (Folder) r;
        } else {
            if( r instanceof Folder ) {
                return (Folder) r;
            } else {
                log.warn( "trashfolder is not of type Folder. Is a : " + r.getClass() );
                return null;
            }
        }
    }

    /**
     * 
     * @return - the secureRead defined on this Web. Not recursive
     */
    @Override
    public boolean isSecureRead() {
//        log.debug( "isSecureRead: " + this.secureRead);
        return this.secureRead;
    }

    public Folder getThemes() {
        Resource res = this.child( "themes" );
        if( res != null && res instanceof Folder ) {
            return (Folder) res;
        } else {
            Folder parent = this.getParent();
            if( parent == null ) return null;
            Web parentWeb = parent.getWeb();
            if( parentWeb == null ) return null;
            return parentWeb.getThemes();
        }
    }

    public Folder getTemplates() {
        String themeName = selectedThemeName();
        Folder themes = null;
        if( themeName != null && themeName.length() > 0 ) {
//            log.debug( "looking for themes: " + themeName);
            themes = getThemes();
        }
        if( themes != null ) {
            Resource res = themes.child( themeName );
            if( res instanceof Folder ) {
//                log.debug( "using theme folder for templates: " + res.getName());
                return (Folder) res;
            }
        }
        Folder templates = (Folder) this.childRes( "templates" );
        if( templates == null ) {
//            log.warn("****** creating new templates folder: " + this.getPath());
            templates = new Folder( this, "templates" );
//            templates.save();                
        }
        return templates;
    }

    /**
     * The template for a web (or host) should always be defined by its parent
     * 
     * This method gets the template from the parentweb
     * 
     * @return
     */
    @Override
    public ITemplate getTemplate() {
        Web parentWeb = getParentWeb();
        if( parentWeb == null ) return null;
        String tn = getTemplateName();
        if( tn != null ) {
            ITemplate t = parentWeb.getTemplate( getTemplateName() );
            if( t == null ) {
//                log.warn("No template for web: " + getTemplateName());
                return null;
            } else {
                return t;
            }
        } else {
            return null;
        }
    }

    @Override
    public void save() {
        super.save();
        getTemplates().save();
    }

    public String selectedThemeName() {
        ThemeSelect sel = getThemeSelect();
        if( sel == null ) return null;
        return sel.getValue();
    }

    public ThemeSelect getThemeSelect() {
        ThemeSelect sel = (ThemeSelect) this.componentMap.get( "theme" );
        if( sel == null ) {
            sel = new ThemeSelect( this, "theme" );
            this.componentMap.add( sel );
        }
        return sel;
    }

    String getEditSourceTemplate() {
        return "";
    }

    @Override
    public Host getHost() {
        if( getParent() == null ) return null;
        Host h = getParent().getHost();
        if( h == null ) log.warn( "no host for: " + this.getPath() );
        return h;
    }

    @Override
    protected void afterSave() {
        super.afterSave();
        Folder templates = this.getTemplates();
        templates.save();
    }
}
