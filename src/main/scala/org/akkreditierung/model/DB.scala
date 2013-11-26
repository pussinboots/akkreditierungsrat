package org.akkreditierung.model

import java.sql.Connection

import scalikejdbc.{ConnectionPoolSettings, ConnectionPool}
import anorm._
import java.net.URI
import scala.util.Properties
import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.slick.session.Database
import scala.slick.driver.{ExtendedProfile, H2Driver, MySQLDriver}
import Database.threadLocalSession
import org.akkreditierung.model.slick.DAL

//https://github.com/slick/slick-examples/blob/master/src/main/scala/com/typesafe/slick/examples/lifted/MultiDBCakeExample.scala

object DB {

  lazy val db = sys.props.get("Database").getOrElse("mysql") match {
    case "mysql" => DB.getSlickMysqlConnection()
    case "h2" => DB.getSlickHSQLDatabase()
  }
  lazy val dal = sys.props.get("Database").getOrElse("mysql") match {
    case "mysql" => new DAL(MySQLDriver)
    case "h2" => new DAL(H2Driver)
  }

  def dbConfigUrl: String = {
    val p = Properties.envOrElse("CLEARDB_DATABASE_URL", "mysql://root:root@127.0.0.1:3306/heroku_9852f75c8ae3ea1")
    println(p)
    p
  }

  def withConnection[A](block: Connection => A): A = {
    val connection: Connection = ConnectionPool.borrow()
    try {
      block(connection)
    }
    finally {
      try {
        connection.close()
      }
    }
  }

  def getConfiguredMysqlConnection() = getMysqlConnection()

  def WithSSL() {
    System.setProperty("javax.net.ssl.keyStore", "keystore")
    System.setProperty("javax.net.ssl.keyStorePassword", "Korn4711")
    System.setProperty("javax.net.ssl.trustStore", "truststore")
    System.setProperty("javax.net.ssl.trustStorePassword", "Korn4711")
  }

  def getMysqlConnection(jdbcUrl: String = dbConfigUrl) {
    Class.forName("com.mysql.jdbc.Driver")
    val dbConnectionInfo = parseDbUrl(jdbcUrl)
    ConnectionPool.singleton(dbConnectionInfo._1, dbConnectionInfo._2, dbConnectionInfo._3, ConnectionPoolSettings(validationQuery = "SELECT 1", initialSize= 10, maxSize = 15))
  }

  def getSlickMysqlConnection(jdbcUrl: String = dbConfigUrl) = {
    val dbConnectionInfo = parseDbUrl(jdbcUrl)
    val ds = new ComboPooledDataSource
    ds.setDriverClass("com.mysql.jdbc.Driver")
    ds.setJdbcUrl(dbConnectionInfo._1)
    ds.setUser(dbConnectionInfo._2)
    ds.setPassword(dbConnectionInfo._3)
    ds.setMaxPoolSize(15)
    ds.setPreferredTestQuery("")
    Database.forDataSource(ds)
  }

  def getSlickHSQLDatabase(jdbcUrl: String = "jdbc:hsqldb:mem:test1") = {
    val ds = new ComboPooledDataSource
    ds.setDriverClass("org.hsqldb.jdbc.JDBCDriver")
    ds.setJdbcUrl(jdbcUrl + ";sql.enforce_size=false")
    Database.forDataSource(ds)
  }

  def createSlickTables(db: Database, dal: DAL) {
    import dal._
    import dal.profile.simple._
    db withSession {
      dal.create
    }
  }

  def createTables() {
    DB.withConnection {
      implicit connection: Connection =>
        SQL("create table studiengaenge (id integer primary key Identity, jobId integer, fach varchar(256), abschluss varchar(256), hochschule varchar(256), bezugstyp varchar(256), link varchar(256), checksum varchar(128), GutachtenLink varchar(256) default null, createDate timestamp default current_timestamp, updateDate timestamp default null, modifiedDate timestamp default null, sourceId integer)").execute()
        SQL("create table studiengaenge_attribute (id integer, k varchar(128), v CLOB)").execute()
        SQL("create table jobs (id integer primary key Identity, createDate timestamp DEFAULT CURRENT_TIMESTAMP, newEntries integer DEFAULT '0', status varchar(12))").execute()
        SQL("create table sources (id integer primary key Identity, name varchar(128), createDate timestamp DEFAULT CURRENT_TIMESTAMP)").execute()
    }
  }

  def getHSqlConnection(jdbcUrl: String = "jdbc:hsqldb:mem", schema: String = "public") = {
    Class.forName("org.hsqldb.jdbc.JDBCDriver")
    ConnectionPool.singleton(s"${jdbcUrl}:${schema}", "", "")
    s"${jdbcUrl}:${schema}"
  }

  def shutdownHSqlConnection(jdbcUrl: String = "jdbc:hsqldb:mem", schema: String = "public") {
    Class.forName("org.hsqldb.jdbc.JDBCDriver")
    if (ConnectionPool.isInitialized()) {
      val connection = ConnectionPool.borrow()
      val statement = connection.createStatement()
      try {
        statement.execute(s"DROP SCHEMA ${schema} CASCADE")
        connection.commit()
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        statement.close()
      }
      ConnectionPool.closeAll()
    }
  }

  def parseConfiguredDbUrl() = parseDbUrl()

  def parseDbUrl(mysqlUrl: String = dbConfigUrl) = {
    val dbUri = new URI(mysqlUrl);

    val username = dbUri.getUserInfo().split(":").head
    val password = dbUri.getUserInfo().split(":").last
    val port = if (dbUri.getPort() == -1) "" else s":${dbUri.getPort()}"

    val dbUrl = "jdbc:mysql://" + dbUri.getHost() + port + dbUri.getPath() + "?useSSL=true&useUnicode=yes&characterEncoding=UTF-8"
    (dbUrl, username, password)
  }
}