package org.akkreditierung.test

import org.specs2.mutable.Before
import org.akkreditierung.model.DB
import scala.slick.session.Database
import org.akkreditierung.model.slick.DAL
import scala.slick.driver.H2Driver

trait SlickDbBefore extends Before {
  override def before {
    val schema="test"
    val db = DB.getSlickHSQLDatabase()
    //EBean.initDataSource("org.hsqldb.jdbc.JDBCDriver", DB.getHSqlConnection())
    //DB.createTables()
    //DB.createSlickTables(new DAL(H2Driver))
    initTestData(db)
  }

  def initTestData(db:Database) {}
}
