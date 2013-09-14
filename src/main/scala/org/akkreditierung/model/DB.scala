package org.akkreditierung.model

import java.sql.Connection

import scalikejdbc.ConnectionPool
import anorm._
import java.net.URI
import scala.util.Properties

object DB {
  val dbConfigUrl: String = Properties.envOrElse("CLEARDB_DATABASE_URL", "mysql://root:root@127.0.0.1:3306/heroku_97e132547a4cac4")

  def withConnection[A](block: Connection => A): A = {
    val connection: Connection = ConnectionPool.borrow()
    try { block(connection) }
    finally { try { connection.close() } }
  }

  def getMysqlConnection(jdbcUrl: String = dbConfigUrl) {
    Class.forName("com.mysql.jdbc.Driver")
    val dbConnectionInfo = parseDbUrl(jdbcUrl)
    //println(s"connect to ${dbConnectionInfo._1}")
    ConnectionPool.singleton(dbConnectionInfo._1, dbConnectionInfo._2, dbConnectionInfo._3)
  }

  def createTables() {
    DB.withConnection {
      implicit connection: Connection =>
        SQL("create table studiengaenge (id integer primary key Identity, fach varchar(256), abschluss varchar(256), hochschule varchar(256), bezugstyp varchar(256), link varchar(256), checksum varchar(128))").execute()
        SQL("create table studiengaenge_attribute (id integer, k varchar(128), v CLOB)").execute()
    }
  }

  def getHSqlConnection(jdbcUrl: String = "jdbc:hsqldb:mem:hsqldb:WithAnorm") {
    Class.forName("org.hsqldb.jdbc.JDBCDriver")
    ConnectionPool.singleton(jdbcUrl, "", "")
  }

  def parseConfiguredDbUrl() = parseDbUrl()

  def parseDbUrl(mysqlUrl: String = dbConfigUrl) = {
    val dbUri = new URI(mysqlUrl);

    val username = dbUri.getUserInfo().split(":").head
    val password = dbUri.getUserInfo().split(":").last
    val port = if (dbUri.getPort() == -1) "" else ":" + dbUri.getPort()

    val dbUrl = "jdbc:mysql://" + dbUri.getHost() + port + dbUri.getPath()
    (dbUrl, username, password)
  }
}