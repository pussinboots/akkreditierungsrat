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

		int timeout = (int) Duration.ONE_HOUR.getMilliseconds();

		Server server = new Server();
		SocketConnector connector = new SocketConnector();

		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(timeout);
		connector.setSoLingerTime(-1);

        int port = Integer.valueOf(Properties.envOrElse("PORT", "8081"));

		connector.setPort(port);
		server.addConnector(connector);

		Resource keystore = Resource.newClassPathResource("/keystore");
		if (keystore != null && keystore.exists()) {
			// if a keystore for a SSL certificate is available, start a SSL
			// connector on port 8443.
			// By default, the quickstart comes with a Apache Wicket Quickstart
			// Certificate that expires about half way september 2021. Do not
			// use this certificate anywhere important as the passwords are
			// available in the source.

			connector.setConfidentialPort(8443);

			SslContextFactory factory = new SslContextFactory();
			factory.setKeyStoreResource(keystore);
			factory.setKeyStorePassword("wicket");
			factory.setTrustStoreResource(keystore);
			factory.setKeyManagerPassword("wicket");
			SslSocketConnector sslConnector = new SslSocketConnector(factory);
			sslConnector.setMaxIdleTime(timeout);
			sslConnector.setPort(8444);
			sslConnector.setAcceptors(4);
			server.addConnector(sslConnector);

			System.out.println("SSL access to the quickstart has been enabled on port 8443");
			System.out.println("You can access the application using SSL on https://localhost:8443");
			System.out.println();
		}

		WebAppContext bb = new WebAppContext();
		bb.setServer(server);
		bb.setContextPath("/");
		bb.setWar("src/main/webapp");

		// START JMX SERVER
		// MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		// server.getContainer().addEventListener(mBeanContainer);
		// mBeanContainer.start();

		server.setHandler(bb);

		try {
			System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
			server.start();
			System.in.read();
			System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
			server.stop();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void initDataSource() {

		ServerConfig config = new ServerConfig();
		config.setName("localhost");
		config.setDdlGenerate(false);
		config.setDdlRun(false);

		DataSourceConfig dataSourceConfig = new DataSourceConfig();
        String dbConfigUrl = Properties.envOrElse("CLEARDB_DATABASE_URL", "mysql://root:root@127.0.0.1:3306/heroku_97e132547a4cac4");
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
		EbeanServerFactory.create(config);
	}
}
