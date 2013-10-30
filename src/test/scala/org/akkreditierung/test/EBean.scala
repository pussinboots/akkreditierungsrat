package org.akkreditierung.test

import com.avaje.ebean.config.{DataSourceConfig, ServerConfig}
import org.akkreditierung.model.DB
import com.avaje.ebean.{EbeanServerFactory, Transaction}
import com.avaje.ebeaninternal.server.lib.ShutdownManager

object EBean {
  def initDataSource(driverClass: String, jdbcUrl: String) {
    Class.forName("org.hsqldb.jdbc.JDBCDriver")
    val config: ServerConfig = new ServerConfig
    config.setName("localhost")
    config.setDdlGenerate(false)
    config.setDdlRun(false)
    val dataSourceConfig: DataSourceConfig = new DataSourceConfig
    dataSourceConfig.setUsername("")
    dataSourceConfig.setPassword("")
    dataSourceConfig.setUrl(jdbcUrl)
    dataSourceConfig.setDriver(driverClass)
    dataSourceConfig.setMinConnections(1)
    dataSourceConfig.setMaxConnections(25)
    dataSourceConfig.setHeartbeatSql("Select 1")
    dataSourceConfig.setIsolationLevel(Transaction.READ_COMMITTED)
    config.setDataSourceConfig(dataSourceConfig)
    config.setDefaultServer(true)
    config.setDdlGenerate(false)
    config.setName("akkreditierungsrat")
    config.setDebugSql(true)
    EbeanServerFactory.create(config)
  }

  def shutdown() {
    ShutdownManager.shutdown()
    Class.forName("org.hsqldb.jdbc.JDBCDriver")
  }
}
