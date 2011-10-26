package com.ettrema.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.web.children.ThemeFinder;
import com.ettrema.web.component.ThemeSelect;
import com.ettrema.web.wall.Wall;
import com.ettrema.web.wall.WallItem;
import com.ettrema.web.wall.WallService;
import java.util.Collections;
import java.util.List;

import static com.ettrema.context.RequestContext._;

@BeanPropertyResource( "clyde" )
public class Web extends Folder {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Web.class );
    private static final long serialVersionUID = 1L;

    public static Web find( Templatable t ) {
        if( t == null ) return null;
        if( t instanceof Web ) return (Web) t;
        return find( t.getParent() );
    }
    public static final String TRASH_FOLDER_NAME = "Trash";
    public static final String RECENT_FOLDER_NAME = "Recent";
    private transient Folder recent;

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
                throw new RuntimeException( ex );
            } catch( NotAuthorizedException ex ) {
                throw new RuntimeException( ex );
            } catch( BadRequestException ex ) {
                throw new RuntimeException( ex );
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

    @Override
    final public boolean isInTemplates() {
        return false;
    }



    public Folder getRecentFolder() {
        return getRecentFolder( false );
    }

    public Folder getRecentFolder( boolean create ) {
        log.trace( "getRecentFolder: " + create + " --" + this.getHref() );
        if( recent != null ) {
            log.trace( "already got a recent" );
            return recent;
        }
        Resource r = this.child( RECENT_FOLDER_NAME );
        if( r == null ) {
            if( create ) {
                try {
                    r = this.createCollection( RECENT_FOLDER_NAME, false );
                } catch( ConflictException ex ) {
                    throw new RuntimeException( ex );
                } catch( NotAuthorizedException ex ) {
                    throw new RuntimeException( ex );
                } catch( BadRequestException ex ) {
                    throw new RuntimeException( ex );
                }
                recent = (Folder) r;
                return recent;
            } else {
                log.trace( "recent folder does not exist, and create is false" );
                return null;
            }
        } else {
            if( r instanceof Folder ) {
                recent = (Folder) r;
                return recent;
            } else {
                log.warn( "RECENT_FOLDER_NAME is not of type Folder. Is a : " + r.getClass() );
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
        Folder themeFolder = _( ThemeFinder.class ).getThemeFolder( this );
        if( themeFolder != null ) {
            return themeFolder;
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

    ITemplate createTemplate( String name, String baseTemplate ) {
        Template t = new Template( getTemplates(), name );
        t.setTemplateName( baseTemplate );
        t.save();
        return t;
    }

    public List<WallItem> getWall() {
        log.warn( "getWall");
        Wall wall = _(WallService.class).getWebWall( this,false );
        if( wall == null ) {
            return Collections.emptyList();
        } else {
            List<WallItem> list = wall.getItems();
            log.warn("wall items: " + list.size());
            return list;
        }
    }

}
