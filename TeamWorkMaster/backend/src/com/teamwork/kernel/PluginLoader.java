package com.teamwork.kernel;

import com.teamwork.contract.IPlugin;
import com.teamwork.core.GlobalExceptionHandler;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader {
    private List<IPlugin> loadedPlugins = new ArrayList<>();

   
    public void loadPluginClass(String className) {
        try {
        
            Class<?> clazz = Class.forName(className);

         
            if (IPlugin.class.isAssignableFrom(clazz)) {
               
                IPlugin plugin = (IPlugin) clazz.getDeclaredConstructor().newInstance();

                loadedPlugins.add(plugin);
            } else {
      
                throw new Exception("Lớp [" + className + "] không hợp lệ vì không implement IPlugin.");
            }
        } catch (Exception e) {
        
            GlobalExceptionHandler.handle("PluginLoader", e);
        }
    }

    public List<IPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}