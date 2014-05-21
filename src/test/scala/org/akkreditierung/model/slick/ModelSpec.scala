package org.akkreditierung.model.slick

import org.specs2.mutable.Specification
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import java.util.Calendar
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
  db withDynSession {
    val now = new Timestamp(Calendar.getInstance().getTimeInMillis)
    dao.drop
    dao.create
    dao.studienGanginsert(Studiengang(Some(1), Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), Some(now), None, 1))
    dao.studiengangAttributes.insert(StudiengangAttribute(1, "fach", "fach"))
    dao.studiengangAttributes.insert(StudiengangAttribute(1, "tel", "0123456789"))
    dao.studiengangAttributes.insert(StudiengangAttribute(1, "www", "www.fach.de"))
    dao.studienGanginsert(Studiengang(Some(2), Some(1), "fach2", "abschluss2", "hochschule2", "bezugstyp2", Some("link"), Some("gutachtenlink"), Some(now), DateUtil.nowDateTimeOpt(), 2))
    dao.studiengangAttributes.insert(StudiengangAttribute(2, "fach", "fach2"))
    dao.studiengangAttributes.insert(StudiengangAttribute(2, "tel", "9876543210"))
    dao.studiengangAttributes.insert(StudiengangAttribute(2, "www", "www.fach2.de"))
  }

  "Slick Models" should {
    "checksum should be the same" in {
      val studiengang = Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, DateUtil.nowDateTimeOpt(), None, 1)
      studiengang.checkSum must beEqualTo("020dcf7e6749afba8a4301843f958302")
    }
    "Studiengang object" should {
      "check that 2 studiengaenge are stored in the database" in {
        db withDynSession {
          (studiengangs.length).run must beEqualTo(2)
        }
      }
      "retrieve one studiengaenge by fach from database" in {
        db withDynSession {
          import QueryTapper._
          findByFach("fach2").cache(_.first)
          findByFach("fach2").cache(_.first)
          val studienGang = findByFach("fach2").cache(_.first)
          studienGang.fach must beEqualTo("fach2")
//          studienGang.attributes.size must beEqualTo(3)
//          studienGang.attributes.get("fach").get.value must beEqualTo("fach2")
        }
      }
      "update updateDate field of one studienganag" in {
        db withDynSession {
          val now = DateUtil.nowDateTimeOpt()
          findByFach("fach2").map(ab => ab.updateDate).update(now)
          val studienGang = findByFach("fach2").first()
          studienGang.updateDate.get.getTime must beEqualTo(now.get.getTime)
        }
      }
      "insert a new studienganag" in {
        db withDynSession {
          val studiengang = studienGanginsert(Studiengang(None, Some(1), "Zahnarzt", "Master", "Zahnfee Academy", "bezugstyp", Some("link"), None, DateUtil.nowDateTimeOpt(), None, 1))
          studiengang.id mustNotEqual(None)
        }
      }
      "denie duplicate insertion of a new studienganag" in {
        db withDynSession {
          val studiengang = studienGanginsert(Studiengang(None, Some(1), "Zahnarzt", "Bachelor", "Zahnfee Academy", "bezugstyp", Some("link"), None, DateUtil.nowDateTimeOpt(), None, 1))
	  studienGanginsert(Studiengang(None, Some(1), "Zahnarzt", "Bachelor", "Zahnfee Academy", "bezugstyp", Some("link"), None, DateUtil.nowDateTimeOpt(), None, 1)) must throwA[Exception]
        }
      }
    }
    "Job object" should {
      "store Job entry with newEntries bigger than zero" in {
        db withDynSession {
          jobInsert(Job(-1, DateUtil.nowDateTime(), 0, "start"))
          updateOrDelete(Job(id = 1, newEntries = 1, status = "finished"))
          val job = findLatestJob().get
          job.newEntries must beEqualTo(1)
          job.status must beEqualTo("finished")
          dao.jobs.withFilter(_.id === job.id).delete
          dao.findLatestJob() must beEqualTo(None)
        }
      }
      "find all existing jobs" in {
        db withDynSession {
          jobInsert(Job(-1, DateUtil.nowDateTime(), 1, "start"))
          jobInsert(Job(-1, DateUtil.nowDateTime(), 4, "finished"))
          jobs.length.run must beEqualTo(2)
          val jobsDB = jobs.list()
          jobsDB.size must beEqualTo(2)
          jobs.delete
          dao.findLatestJob() must beEqualTo(None)
        }
      }
      "delete Job entry with newEntries equal zero" in {
        db withDynSession {
          val job = jobInsert(Job(-1, DateUtil.nowDateTime(), 1, "start"))
          updateOrDelete(job.copy(newEntries = 0))
          findLatestJob() must beEqualTo(None)
        }
      }
    }
    "Source object" should {
      "find or create source akkreditierungsrat" in {
        db withDynSession {
          val source = findOrInsert(SourceAkkreditierungsRat.name).get
          sources.withFilter(_.id === source.id).delete
          source.name must beEqualTo(SourceAkkreditierungsRat.name)
        }
      }
      "store source and retrieve it" in {
        db withDynSession {
          val source = sourceInsert(Source(name = SourceAkkreditierungsRat.name))
          source.name must beEqualTo(SourceAkkreditierungsRat.name)
          val retrievedSource = sources.filter(_.name === SourceAkkreditierungsRat.name).first
          sources.withFilter(_.id === retrievedSource.id).delete
          retrievedSource.id must beEqualTo(source.id)
          retrievedSource.name must beEqualTo(source.name)
        }
      }
      "find all existing sources" in {
        db withDynSession {
          sourceInsert(Source(name = SourceAkkreditierungsRat.name))
          sourceInsert(Source(name = "potsdam u"))
          sourceInsert(Source(name = "bremen u"))
          val sourcesDB = sources.list
          sources.delete
          sourcesDB.length must beEqualTo(3)
        }
      }
    }
  }
}
