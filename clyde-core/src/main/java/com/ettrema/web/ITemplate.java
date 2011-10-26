package com.ettrema.web;

import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.security.PermissionRecipient.Role;
import com.ettrema.web.security.Subject;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public interface ITemplate extends Templatable{
    



    public enum DocType {
        STRICT,
        TRANSITIIONAL,
        XSTRICT,
        XTRANSITIONAL
    }

    Folder createFolderFromTemplate( Folder location, String name );

    BaseResource createPageFromTemplate( Folder location, String name, InputStream in, Long length );

    /**
     *
     * @param location
     * @param name
     * @return - a newly created, but not saved, baseresource
     */
    BaseResource createPageFromTemplate( Folder location, String name );

    Component _invoke(String name);

    String render( RenderContext child );

    ComponentDef getComponentDef( String name );

    ComponentDefMap getComponentDefs();

    /**
     * Return true if this template represents an entity of the given type
     *
     * For example, a template for a web page should return true for type='page'
     * , but should return false for type='image'
     *
     * A template which represents a folder should return true for type='folder',
     * but false for type='page'
     *
     * Note that this is slightly different to the 'is' method. The difference is
     * that 'is' asks if the entity itself is of a certain type, whereas 'represents'
     * asks if an instance of a template is of a certain type.
     *
     * In general, implementations should be lenient, ignoring case and file name
     * extensions
     *
     * @param type
     * @return
     */
    boolean represents( String type );

    /**
     * Tells if this template can create instances of folders (aka collections)
     *
     * @return
     */
    boolean canCreateFolder();

    /**
     * Called when a page which is an instance of this template receives a POST
     * 
     * Return a url to do a redirect, or null to not redirect and continue processing
     * 
     * @param aThis 
     */
    String onPost(CommonTemplated aThis);
    
    /**
     * Called by the resource just before save is called
     * 
     * @param aThis
     */
    void onBeforeSave(BaseResource aThis);

    /**
     * Called by the resource immediately after it has been saved
     *
     * @param aThis
     */
    void onAfterSave( BaseResource aThis );

    /**
     * The type of HTML document this will produce
     *
     * @return
     */
    public DocType getDocType();
    
    /**
     * If true, will make instances require a valid user
     * 
     * @return 
     */
    public Boolean isSecure();

    
    /**
     * Non-recursive check to see if the given subject has the requested Role on the resource,
     * where the resource is an instance of this template.
     * 
     * This will check programmatic rules, rather then persisted relationships. If
     * there is no applicable rule, so this template has no opinion, it will return
     * null.
     * 
     * @param user
     * @param role
     * @param target
     * @return - true indicates the subject does have the requested role, false it does not
     * and null indicates this template has no opinion
     */
    Boolean hasRole(Subject user, Role role, CommonTemplated target);

}
