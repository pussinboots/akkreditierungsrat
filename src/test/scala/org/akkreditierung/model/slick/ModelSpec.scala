package org.akkreditierung.model.slick

import org.specs2.mutable.{Before, Specification}
import java.sql.Date
import scala.Some
import scala.slick.session.Database
import scala.slick.driver.H2Driver.simple._
import java.util.Calendar

class ModelSpec extends Specification with Before {


  "Slick Models" should {
    "checksum should be the same" in {
      val studiengang = new Studiengang(None, Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(new Date(Calendar.getInstance().getTimeInMillis)), 1)
      studiengang.checkSum must beEqualTo("020dcf7e6749afba8a4301843f958302")
    }
    "Studiengang object" should {
      Database.forURL("jdbc:hsqldb:mem:test1;sql.enforce_size=false", driver = "org.hsqldb.jdbc.JDBCDriver") withSession {
        Studiengangs.insert(Studiengang(Some(1), Some(1), "fach", "abschluss", "hochschule", "bezugstyp", Some("link"), None, Some(new Date(Calendar.getInstance().getTimeInMillis)), 1))
        StudiengangAttributes.insert(StudiengangAttribute(1, "fach", "fach"))
        StudiengangAttributes.insert(StudiengangAttribute(1, "tel", "0123456789"))
        StudiengangAttributes.insert(StudiengangAttribute(1, "www", "www.fach.de"))
        Studiengangs.insert(Studiengang(Some(2), Some(1), "fach2", "abschluss2", "hochschule2", "bezugstyp2", Some("link"), Some("gutachtenlink"), Some(new Date(Calendar.getInstance().getTimeInMillis)), 2))
        StudiengangAttributes.insert(StudiengangAttribute(2, "fach", "fach2"))
        StudiengangAttributes.insert(StudiengangAttribute(2, "tel", "9876543210"))
        StudiengangAttributes.insert(StudiengangAttribute(2,  "www", "www.fach2.de"))
      "retrieve all studiengaenge from database" in {
        Studiengangs.length must beEqualTo(2)
      }
      "retrieve one studiengaenge by fach from database" in {
        val studienGang = (for {a <- Studiengangs if (a.fach == "fach")} yield (a)).first()
        studienGang.fach must beEqualTo("fach2")
        studienGang.attributes.size must beEqualTo(3)
        studienGang.attributes.get("fach").get.value must beEqualTo("fach2")
      }
    }
  }
}
}
