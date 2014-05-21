package tools

import scala.slick.driver.MySQLDriver
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

object DBMigration extends App {
  val db = DB.getSlickMysqlConnection()
  val dao = new DAL(MySQLDriver)
  import dao._
  import dao.profile.simple._

  db withDynSession {
    println("create tables")
    dao.create
  }
}
