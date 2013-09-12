package org.akkreditierung.model

import java.sql.Connection

import scalikejdbc.ConnectionPool
import org.slf4j.LoggerFactory
import anorm._
import java.net.URI

object DB {
  def withConnection[A](block: Connection => A): A = {
    val connection: Connection = ConnectionPool.borrow()

    try {
      block(connection)
    } finally {
      try {
        connection.close()
      }
    }
  }

  private def logger = LoggerFactory.getLogger(getClass)

  def getMysqlConnection(jdbcUrl: Option[String]) {
    Class.forName("com.mysql.jdbc.Driver")
    val dbConnectionInfo = parseMySQLUrl(jdbcUrl.getOrElse("mysql://root:root@localhost:3306/heroku_9852f75c8ae3ea1"))
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
    //ConnectionPool.singleton("jdbc:hsqldb:file:data/db", "", "")
  }

  def parseMySQLUrl(mysqlUrl: String) = {
    val dbUri = new URI(mysqlUrl);

    val username = dbUri.getUserInfo().split(":").head
    val password = dbUri.getUserInfo().split(":").last
    val port = {
      if (dbUri.getPort() == -1) "" else ":" + dbUri.getPort()
    }
    val dbUrl = "jdbc:mysql://" + dbUri.getHost() + port + dbUri.getPath()
    logger.info(Seq(dbUrl, username, password).toString())
    (dbUrl, username, password)
  }
}