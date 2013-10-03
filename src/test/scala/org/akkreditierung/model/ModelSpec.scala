package org.akkreditierung.model

import org.specs2.mutable._

class ModelSpec extends Specification with Before {

  def before = {
    DB.getHSqlConnection("jdbc:hsqldb:mem:hsqldb:ModelSpec")
    DB.createTables()
    Studiengang.Insert(new Studiengang(None, "fach", "abschluss", "hochschule", "bezugstyp", "link", None))
    Studiengang.Insert(new Studiengang(None, "fach2", "abschluss2", "hochschule2", "bezugstyp2", "link", Some("gutachtenlink")))
  }

  "Studiengang class" should {
    "checksum should be the same" in {
      val studiengang = new Studiengang(None, "fach", "abschluss", "hochschule", "bezugstyp", "link", None)
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
}
