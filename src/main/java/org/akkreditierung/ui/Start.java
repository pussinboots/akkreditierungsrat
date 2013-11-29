package org.akkreditierung.ui;

import org.akkreditierung.model.DB;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import scala.util.Properties;

public class Start {
	public static void main(String[] args) throws Exception {
        DB.WithSSL();
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
