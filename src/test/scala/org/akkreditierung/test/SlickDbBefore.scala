package org.akkreditierung.test

import org.specs2.mutable.Before
import org.akkreditierung.model.DB
import scala.slick.session.Database
import Database.threadLocalSession

trait SlickDbBefore extends Before {
  sys.props.+=("Database" -> "h2")

  override def before {
    val schema="test"
    val db = DB.getSlickHSQLDatabase()
    db withSession DB.dal.recreate
    initTestData(db)
  }

  def initTestData(db:Database) {}
}
