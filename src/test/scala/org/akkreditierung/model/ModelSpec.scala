package org.akkreditierung.model

import org.specs2.mutable._
import org.akkreditierung.test.HSQLDbBefore
import java.util.Date
import anorm._
import scala.Some
import anorm.SqlParser._
import anorm.~
import scala.Some

class ModelSpec extends Specification with HSQLDbBefore {
  sequential

  override def initTestData() {
    val studienGang1 = Studiengang.Insert(new Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(new Date()), 1))
    StudiengangAttribute.Insert(new StudiengangAttribute(studienGang1.id.get, "fach", "fach"))
    StudiengangAttribute.Insert(new StudiengangAttribute(studienGang1.id.get, "tel", "0123456789"))
    StudiengangAttribute.Insert(new StudiengangAttribute(studienGang1.id.get, "www", "www.fach.de"))
    val studienGang2 = Studiengang.Insert(new Studiengang(None, Some(2), "fach2", "abschluss2", "hochschule2", "bezugstyp2", Some("link"), Some("gutachtenlink"), Some(new Date()), 2))
    StudiengangAttribute.Insert(new StudiengangAttribute(studienGang2.id.get, "fach", "fach2"))
    StudiengangAttribute.Insert(new StudiengangAttribute(studienGang2.id.get, "tel", "9876543210"))
    StudiengangAttribute.Insert(new StudiengangAttribute(studienGang2.id.get, "www", "www.fach2.de"))
  }

  "Studiengang class" should {
    "checksum should be the same" in {
      val studiengang = new Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(new Date()), 1)
      studiengang.checkSum must beEqualTo("020dcf7e6749afba8a4301843f958302")
    }
  }

  "Studiengang object" should {
    "retrieve all studiengaenge from database" in {
      Studiengang.findAll().length must beEqualTo(2)
    }
    "retrieve one studiengaenge by fach from database" in {
      val studienGang = Studiengang.findByFach("fach2")
      studienGang.fach must beEqualTo("fach2")
      studienGang.studiengangAttributes.size must beEqualTo(3)
      studienGang.studiengangAttributes.get("fach").get.value must beEqualTo("fach2")
    }
    "update updateDate field of one studienganag" in {
      val studienGang = Studiengang.findByFach("fach")
      val now = new Date()
      Studiengang.UpdateUpdateDate(now, studienGang)
      val updateDate = DB.withConnection {
        implicit connection =>
          SQL("select updateDate from studiengaenge where fach={fach}").on(
            'fach -> studienGang.fach).single({get[Date]("studiengaenge.updateDate") map { case updateDate => updateDate}})
      }

      updateDate.getTime must beEqualTo(now.getTime)
    }
    "update modiefiedDate field of one studiengaenge" in {
      val studienGang = Studiengang.findByFach("fach")
      val now = new Date()
      studienGang.modifiedDate=Some(now)
      Studiengang.UpdateModifiedDate(studienGang)
      val studienGangChanged = Studiengang.findByFach("fach")

      studienGangChanged.modifiedDate.get.getTime must beEqualTo(now.getTime)
    }
  }

  "StudiengangAttribute object" should {
    "retrieve all StudiengangAttributes from database" in {
      StudiengangAttribute.findAll().length must beEqualTo(6)
    }
    "retrieve one studiengaenge by fach from database" in {
      val attribute = Studiengang.findByFach("fach2").studiengangAttributes.get("fach").get
      attribute.value must beEqualTo("fach2")
      StudiengangAttribute.Update(new StudiengangAttribute(id=attribute.id, key=attribute.key, value="fach2 erweitert"))
      Studiengang.findByFach("fach2").studiengangAttributes.get("fach").get.value must beEqualTo("fach2 erweitert")
    }
  }

  "Job object" should {
    "store Job entry with newEntries bigger than zero" in {
      val job = Job.Insert(Job())
      Job.UpdateOrDelete(Job(id=job.id, newEntries = 1, status="finished"))
      Job.findLatest().get.newEntries must beEqualTo(1)
      Job.findLatest().get.status must beEqualTo("finished")
      Job.Delete(job)
      Job.findLatest() must beEqualTo(None)
    }
    "find all existing jobs" in {
      val job = Job.Insert(Job())
      Job.Insert(Job())
      Job.UpdateOrDelete(Job(id=job.id, newEntries = 1, status="finished"))
      val jobs = Job.findAll()
      jobs.size must beEqualTo(2)
      jobs.foreach(job=>Job.Delete(job))
      Job.Delete(job)
      Job.findLatest() must beEqualTo(None)
    }
    "delete Job entry with newEntries equal zero" in {
      val job = Job.Insert(Job())
      Job.UpdateOrDelete(Job(id=job.id, newEntries = 0, status="finished"))
      Job.findLatest() must beEqualTo(None)
    }
  }

  "Source object" should {
    "find or create source akkreditierungsrat" in {
      val source = Source.FindOrCreateSourceAkkreditierungsrat()
      Source.Delete(source.get)
      source.get.name must beEqualTo(SourceAkkreditierungsRat.name)
    }
    "store source and retrieve it" in {
      val source = Source.Insert(Source(name=SourceAkkreditierungsRat.name))
      source.name must beEqualTo(SourceAkkreditierungsRat.name)
      val retrievedSource = Source.FindByName(SourceAkkreditierungsRat.name)
      Source.Delete(retrievedSource.get)
      retrievedSource.get.id must beEqualTo(source.id)
      retrievedSource.get.name must beEqualTo(source.name)
    }
    "find all existing sources" in {
      Source.Insert(Source(name=SourceAkkreditierungsRat.name))
      Source.Insert(Source(name="potsdam u"))
      Source.Insert(Source(name="bremen u"))
      Source.FindAll().size must beEqualTo(3)
    }
  }
}
