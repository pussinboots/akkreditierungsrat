package org.akkreditierung.ui.listener;


import com.avaje.ebean.Ebean;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ApplicationLifeCycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Ebean.getServerCacheManager();

    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}

