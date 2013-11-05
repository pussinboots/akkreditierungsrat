package org.akkreditierung.model

import org.specs2.mutable._
import org.akkreditierung.test.HSQLDbBefore

class ModelSpec extends Specification with HSQLDbBefore {
  sequential

  override def initTestData() {
    Studiengang.Insert(new Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, 1))
    Studiengang.Insert(new Studiengang(None, Some(2), "fach2", "abschluss2", "hochschule2", "bezugstyp2", Some("link"), Some("gutachtenlink"), 2))
  }

  "Studiengang class" should {
    "checksum should be the same" in {
      val studiengang = new Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, 1)
      studiengang.checkSum must beEqualTo("020dcf7e6749afba8a4301843f958302")
    }
  }

  "Studiengang object" should {
    "retrieve all studiengaenge from database" in {
      Studiengang.findAll().length must beEqualTo(2)
    }
    "retrieve one studiengaenge by fach from database" in {
      Studiengang.findByFach("fach2").fach must beEqualTo("fach2")
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
