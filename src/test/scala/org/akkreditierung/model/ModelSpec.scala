package org.akkreditierung.model

import org.specs2.mutable._

class ModelSpec extends Specification {

  "Studiengang class" should {
    "checksum should be the same" in {
      val studiengang = new Studiengang(None, "fach", "abschluss", "hochschule", "bezugstyp", "link", None)
      studiengang.checkSum must beEqualTo("020dcf7e6749afba8a4301843f958302")
    }
  }
}
