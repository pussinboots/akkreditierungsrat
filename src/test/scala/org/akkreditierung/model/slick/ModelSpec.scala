package org.akkreditierung.model.slick

import org.specs2.mutable.Specification
import scala.slick.session.Database
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import java.util.Calendar
import org.akkreditierung.model.SourceAkkreditierungsRat
import java.sql.Timestamp
import scala.Some
import org.akkreditierung.DateUtil
import org.akkreditierung.model.DB
import scala.slick.driver.H2Driver

class ModelSpec extends Specification {
  sequential

  val db = DB.getSlickHSQLDatabase()
  val dao = new DAL(H2Driver)
  import dao._
  import dao.profile.simple._
  db withSession {
    val now = new Timestamp(Calendar.getInstance().getTimeInMillis)
    (dao.Studiengangs.ddl ++ dao.StudiengangAttributes.ddl ++ dao.Sources.ddl ++ dao.Jobs.ddl).drop
    (dao.Studiengangs.ddl ++ dao.StudiengangAttributes.ddl ++ dao.Sources.ddl ++ dao.Jobs.ddl).create
    dao.Studiengangs.insert(Studiengang(Some(1), Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(now), None, 1))
    dao.StudiengangAttributes.insert(StudiengangAttribute(1, "fach", "fach"))
    dao.StudiengangAttributes.insert(StudiengangAttribute(1, "tel", "0123456789"))
    dao.StudiengangAttributes.insert(StudiengangAttribute(1, "www", "www.fach.de"))
    dao.Studiengangs.insert(Studiengang(Some(2), Some(1), "fach2", "abschluss2", "hochschule2", "bezugstyp2", Some("link"), Some("gutachtenlink"), Some(now), DateUtil.nowDateTimeOpt(), 2))
    dao.StudiengangAttributes.insert(StudiengangAttribute(2, "fach", "fach2"))
    dao.StudiengangAttributes.insert(StudiengangAttribute(2, "tel", "9876543210"))
    dao.StudiengangAttributes.insert(StudiengangAttribute(2, "www", "www.fach2.de"))
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
//          studienGang.attributes.size must beEqualTo(3)
//          studienGang.attributes.get("fach").get.value must beEqualTo("fach2")
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
      "insert a new studienganag" in {
        db withSession {
          val studiengang = Studiengangs.insert(Studiengang(None, Some(1), "Zahnarzt", "Master", "Zahnfee Academy", "bezugstyp", Some("link"), None, DateUtil.nowDateTimeOpt(), None, 1))
          studiengang.id mustNotEqual(None)
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
          dao.Jobs.withFilter(_.id === job.id).delete
          dao.Jobs.findLatest() must beEqualTo(None)
        }
      }
      "find all existing jobs" in {
        db withSession {
          Jobs.insert(Job(-1, DateUtil.nowDateTime(), 1, "start"))
          Jobs.insert(Job(-1, DateUtil.nowDateTime(), 4, "finished"))
          Query(Jobs.length).first must beEqualTo(2)
          val jobs = Query(Jobs).list()
          jobs.size must beEqualTo(2)
          Query(dao.Jobs).delete
          dao.Jobs.findLatest() must beEqualTo(None)
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
    "Source object" should {
      "find or create source akkreditierungsrat" in {
        db withSession {
          val source = Sources.findOrInsert(SourceAkkreditierungsRat.name).get
          Sources.withFilter(_.id === source.id).delete
          source.name must beEqualTo(SourceAkkreditierungsRat.name)
        }
      }
      "store source and retrieve it" in {
        db withSession {
          val source = Sources.insert(Source(name = SourceAkkreditierungsRat.name))
          source.name must beEqualTo(SourceAkkreditierungsRat.name)
          val retrievedSource = Query(Sources).filter(_.name === SourceAkkreditierungsRat.name).first
          Sources.withFilter(_.id === retrievedSource.id).delete
          retrievedSource.id must beEqualTo(source.id)
          retrievedSource.name must beEqualTo(source.name)
        }
      }
      "find all existing sources" in {
        db withSession {
          Sources.insert(Source(name = SourceAkkreditierungsRat.name))
          Sources.insert(Source(name = "potsdam u"))
          Sources.insert(Source(name = "bremen u"))
          val sources = Query(Sources).list
          Query(Sources).delete
          sources.length must beEqualTo(3)
        }
      }
    }
  }
}
