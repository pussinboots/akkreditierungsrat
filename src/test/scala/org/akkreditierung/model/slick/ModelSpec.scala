package org.akkreditierung.model.slick

import org.specs2.mutable.Specification
import scala.slick.session.Database
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import java.util.Calendar
import org.akkreditierung.model.DB
import java.sql.Timestamp
import scala.Some
import org.akkreditierung.DateUtil

class ModelSpec extends Specification {
  sequential

  val db = DB.getSlickHSQLDatabase()

  db withSession {
    val now = new Timestamp(Calendar.getInstance().getTimeInMillis)
    (Studiengangs.ddl ++ StudiengangAttributes.ddl ++ Sources.ddl ++ Jobs.ddl).create
    Studiengangs.insert(Studiengang(Some(1), Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(now), None, 1))
    StudiengangAttributes.insert(StudiengangAttribute(1, "fach", "fach"))
    StudiengangAttributes.insert(StudiengangAttribute(1, "tel", "0123456789"))
    StudiengangAttributes.insert(StudiengangAttribute(1, "www", "www.fach.de"))
    Studiengangs.insert(Studiengang(Some(2), Some(1), "fach2", "abschluss2", "hochschule2", "bezugstyp2", Some("link"), Some("gutachtenlink"), Some(now), DateUtil.nowDateTimeOpt(), 2))
    StudiengangAttributes.insert(StudiengangAttribute(2, "fach", "fach2"))
    StudiengangAttributes.insert(StudiengangAttribute(2, "tel", "9876543210"))
    StudiengangAttributes.insert(StudiengangAttribute(2, "www", "www.fach2.de"))
  }

  "Slick Models" should {
    "checksum should be the same" in {
      val studiengang = new Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, DateUtil.nowDateTimeOpt(), None, 1)
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
          val now = DateUtil.nowDateTimeOpt()
          Studiengangs.findByFach("fach2").map(ab => ab.updateDate).update(now)
          val studienGang = Studiengangs.findByFach("fach2").first()
          studienGang.updateDate.get.getTime must beEqualTo(now.get.getTime)
        }
      }
    }
    "Job object" should {
      "store Job entry with newEntries bigger than zero" in {
        db withSession {
          Jobs.insert(Job(-1, DateUtil.nowDateTime(), 0, "start"))
          Jobs.updateOrDelete(Job(id = 1, newEntries = 1, status = "finished"))
          val job = Jobs.findLatest().get
          job.newEntries must beEqualTo(1)
          job.status must beEqualTo("finished")
          Jobs.withFilter(_.id === job.id).delete
          Jobs.findLatest() must beEqualTo(None)
        }
      }
      "find all existing jobs" in {
        db withSession {
          Jobs.insert(Job(-1, DateUtil.nowDateTime(), 1, "start"))
          Jobs.insert(Job(-1, DateUtil.nowDateTime(), 4, "finished"))
          Query(Jobs.length).first must beEqualTo(2)
          val jobs = Query(Jobs).list()
          jobs.size must beEqualTo(2)
          Query(Jobs).delete
          Jobs.findLatest() must beEqualTo(None)
        }
      }
      "delete Job entry with newEntries equal zero" in {
        db withSession {
          val job = Jobs.insert(Job(-1, DateUtil.nowDateTime(), 1, "start"))
          Jobs.updateOrDelete(job.copy(newEntries = 0))
          Jobs.findLatest() must beEqualTo(None)
        }
      }
    }
  }
}
