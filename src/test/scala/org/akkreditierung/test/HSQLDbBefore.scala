package org.akkreditierung.test

import org.specs2.mutable.Before
import org.akkreditierung.model.{Studiengang, DB}

trait HSQLDbBefore extends Before {
  override def before {
    val schema="test"
    DB.shutdownHSqlConnection()
    EBean.initDataSource("org.hsqldb.jdbc.JDBCDriver", DB.getHSqlConnection())
    DB.createTables()
    initTestData()
  }

  def initTestData() {}
}
