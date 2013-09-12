package org.akkreditierung.ui;

import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import org.akkreditierung.model.DB;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import scala.util.Properties;

public class Start {
	public static void main(String[] args) throws Exception {
		initDataSource();

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

	private static void initDataSource() {

		ServerConfig config = new ServerConfig();
		config.setName("localhost");
		config.setDdlGenerate(false);
		config.setDdlRun(false);

		DataSourceConfig dataSourceConfig = new DataSourceConfig();
        String dbConfigUrl = Properties.envOrElse("CLEARDB_DATABASE_URL", "mysql://be18d0fb184011:e47f7618@us-cdbr-east-04.cleardb.com/heroku_9852f75c8ae3ea1?reconnect=true");
        String jdbcUrl = DB.parseMySQLUrl(dbConfigUrl)._1();
        String userName = DB.parseMySQLUrl(dbConfigUrl)._2();
        String password = DB.parseMySQLUrl(dbConfigUrl)._3();
        dataSourceConfig.setUsername(userName);
        dataSourceConfig.setPassword(password);
		dataSourceConfig.setUrl(jdbcUrl);
		dataSourceConfig.setDriver("com.mysql.jdbc.Driver");
		dataSourceConfig.setMinConnections(1);
		dataSourceConfig.setMaxConnections(25);
		dataSourceConfig.setHeartbeatSql("Select 1");
		dataSourceConfig.setIsolationLevel(Transaction.READ_COMMITTED);

		config.setDataSourceConfig(dataSourceConfig);
		config.setDefaultServer(true);
		config.setDdlGenerate(true);
		config.setName("akkreditierungsrat");
        config.setDebugSql(true);
		EbeanServerFactory.create(config);
	}
}
