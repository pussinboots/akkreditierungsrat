package org.akkreditierung.model.slick

import org.specs2.mutable.Specification
import scala.slick.session.Database
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import java.util.Calendar
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.akkreditierung.model.DB
import java.sql.{Timestamp, Date}
import anorm._
import anorm.SqlParser._
import scala.Some

class ModelSpec extends Specification {
  val db = {
    val ds = new ComboPooledDataSource
    ds.setDriverClass("org.hsqldb.jdbc.JDBCDriver")
    ds.setJdbcUrl("jdbc:hsqldb:mem:test1;sql.enforce_size=false")
    Database.forDataSource(ds)
  }
  db withSession {
    val now = new Timestamp(Calendar.getInstance().getTimeInMillis)
    (Studiengangs.ddl ++ StudiengangAttributes.ddl).create
    Studiengangs.insert(Studiengang(Some(1), Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(now), None, 1))
    StudiengangAttributes.insert(StudiengangAttribute(1, "fach", "fach"))
    StudiengangAttributes.insert(StudiengangAttribute(1, "tel", "0123456789"))
    StudiengangAttributes.insert(StudiengangAttribute(1, "www", "www.fach.de"))
    Studiengangs.insert(Studiengang(Some(2), Some(1), "fach2", "abschluss2", "hochschule2", "bezugstyp2", Some("link"), Some("gutachtenlink"), Some(now), Some(new Timestamp(Calendar.getInstance().getTimeInMillis)), 2))
    StudiengangAttributes.insert(StudiengangAttribute(2, "fach", "fach2"))
    StudiengangAttributes.insert(StudiengangAttribute(2, "tel", "9876543210"))
    StudiengangAttributes.insert(StudiengangAttribute(2,  "www", "www.fach2.de"))
  }

  "Slick Models" should {
    "checksum should be the same" in {
      val studiengang = new Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(new Timestamp(Calendar.getInstance().getTimeInMillis)), None, 1)
      studiengang.checkSum must beEqualTo("020dcf7e6749afba8a4301843f958302")
    }
    "Studiengang object" should {
      "check that 2 studiengaenge are stored in the database" in {
        db withSession {
          (Studiengangs.length).run must beEqualTo(2)
        }
      }
      "retrieve one studiengaenge by fach from database" in {
        db withSession {
          val studienGang = Studiengangs.findByFach("fach2").first
          studienGang.fach must beEqualTo("fach2")
          studienGang.attributes.size must beEqualTo(3)
          studienGang.attributes.get("fach").get.value must beEqualTo("fach2")
        }
      }
      "update updateDate field of one studienganag" in {
        db withSession {
          val now = new Timestamp(Calendar.getInstance().getTimeInMillis)
          println(Studiengangs.findByFach("fach2").first().updateDate.get.getTime)
          Studiengangs.findByFach("fach2").map(ab => ab.updateDate ~ ab.hochschule).update(Some(now), "schule")
          println(now.getTime)
          println(Studiengangs.findByFach("fach2").map(_.updateDate).updateStatement)
          val studienGang = Studiengangs.findByFach("fach2").first()
          println(studienGang)
          studienGang.updateDate.get.getTime must beEqualTo(now.getTime)
        }
      }
    }
  }
}