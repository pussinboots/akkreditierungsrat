package org.akkreditierung

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable._
import org.akkreditierung.betamax.Betamax
import co.freeside.betamax.TapeMode
import org.akkreditierung.model.{DB, StudiengangAttribute, Studiengang}
import org.akkreditierung.AkkreditierungsRatClient._

@RunWith(classOf[JUnitRunner])
class AkkreditierungsRatClientSpec extends Specification {
  "The AkkreditierungsRat Client" should {
    "get SuperXmlTabelle from playback" in Betamax("akkreditierungsratclient", Some(TapeMode.READ_ONLY)) {
      val result = getResult("72240F2156C40507378CCE3E13F1EE75")
      println(result)
      result.length must beEqualTo(17218)
    }

    "fetch studiengaenge from playback" in {
      "fetch and store studiengaenge in the database" in Betamax("akkreditierungsratclient", Some(TapeMode.READ_ONLY)) {
        DB.getHSqlConnection("jdbc:hsqldb:mem:hsqldb:studiengaenge")
        DB.createTables()
        fetchAndStoreStudienGaenge("72240F2156C40507378CCE3E13F1EE75", 30, {
          studienGang: Studiengang =>
        }).length must beEqualTo(30)
        val studienGaenge = Studiengang.findAll()
        studienGaenge.length must beEqualTo(30)
      }

      "check that 614 studiengang attribute are stored in the database for the 30 fetched studiengaenge" in Betamax("akkreditierungsratclient", Some(TapeMode.READ_ONLY)) {
        DB.getHSqlConnection("jdbc:hsqldb:mem:hsqldb:studiengangattribute")
        DB.createTables()
        fetchAndStoreStudienGaenge("72240F2156C40507378CCE3E13F1EE75", 30, {
          studienGang: Studiengang =>
            fetchAndStoreStudienGangInfo("72240F2156C40507378CCE3E13F1EE75", studienGang)
        }).length must beEqualTo(30)
        val studienGangAttribute = StudiengangAttribute.findAll()
        studienGangAttribute.length must beEqualTo(614)
      }
    }
  }
}