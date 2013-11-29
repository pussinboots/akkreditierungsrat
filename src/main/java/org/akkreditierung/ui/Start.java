package org.akkreditierung.ui;

import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import org.akkreditierung.model.DB;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import scala.Tuple3;
import scala.util.Properties;

public class Start {
	public static void main(String[] args) throws Exception {
        String webappDirLocation = "src/main/webapp/";
        int port = Integer.valueOf(Properties.envOrElse("PORT", "8081"));

        Server server = new Server(port);
        WebAppContext root = new WebAppContext();
        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        root.setParentLoaderPriority(true);

        server.setHandler(root);
        server.start();
        server.join();
	}
}
