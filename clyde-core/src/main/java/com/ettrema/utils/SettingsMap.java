package com.ettrema.utils;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Templatable;
import com.ettrema.web.Web;
import com.ettrema.web.component.ComponentValue;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class SettingsMap extends AbstractDummyMap<String,Object> {

    private static final Path PATH_SETTINGS = Path.path("admin/settings");
    
    private final Templatable target;
    
    private Web web;
    private boolean settingsLoaded;
    private Folder settingsFolder;
    private Map<String,SettingItemMap> itemsMap;

    public SettingsMap(Templatable target) {
        this.target = target;
    }
    
    
    @Override
    public Object get(Object key) {
        if(web == null ) {
            web = target.getWeb();
        }
        System.out.println("web: " + web.getName());
        if( !settingsLoaded ) {
            settingsLoaded = true;
            Resource rSettingsFolder = web.find(PATH_SETTINGS);
            if( rSettingsFolder instanceof Folder ) {
                settingsFolder = (Folder) rSettingsFolder;
            }        
        }
        if( settingsFolder == null ) {
            return null;
        }
        if( itemsMap == null ) {
            itemsMap = new HashMap<>();
        }
        SettingItemMap item = itemsMap.get(key);
        if( item == null ) {
            Templatable settingsItem = settingsFolder.childRes(key.toString());
            item = new SettingItemMap(settingsItem);
            itemsMap.put(key.toString(), item);
        }
        return item;
    }
    
    public class SettingItemMap extends AbstractDummyMap<String,Object> {

        private final Templatable settingsItem;

        public SettingItemMap(Templatable settingsItem) {
            this.settingsItem = settingsItem;
        }
                        
        @Override
        public Object get(Object key) {
            ComponentValue cv = settingsItem.getValues().get(key);
            if( cv == null ) {
                return "";
            } else {
                Object value = cv.getValue();
                if( value == null ) {
                    return "";
                } else {
                    return value;
                }
            }
        }
        
    }
}
