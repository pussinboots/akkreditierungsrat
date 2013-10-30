package org.akkreditierung

import org.specs2.mutable._
import org.akkreditierung.model.{DB, StudiengangAttribute, Studiengang}
import org.akkreditierung.AkkreditierungsRatClient._
import org.akkreditierung.test.Betamax

class AkkreditierungsRatClientSpec extends Specification {

  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true") //activate betamax proxy for dispatch

  "The AkkreditierungsRat Client" should {
    "get SuperXmlTabelle from playback" in Betamax("akkreditierungsratclient") {
      val result = getResult("72240F2156C40507378CCE3E13F1EE75")
      result.length must beEqualTo(17218)
    }

    "fetch studiengaenge from playback" in {
      "fetch and store studiengaenge in the database" in Betamax("akkreditierungsratclient") {
        DB.getHSqlConnection("jdbc:hsqldb:mem:hsqldb:studiengaenge")
        DB.createTables()
        fetchAndStoreStudienGaenge("72240F2156C40507378CCE3E13F1EE75", 30, 30, {studienGang: Studiengang =>}).length must beEqualTo(30)
        Studiengang.findAll().length must beEqualTo(30)
      }

      "check that 614 studiengang attribute are stored in the database for the 30 fetched studiengaenge" in Betamax("akkreditierungsratclient") {
        DB.getHSqlConnection("jdbc:hsqldb:mem:hsqldb:studiengangattribute")
        DB.createTables()
        fetchAndStoreStudienGaenge("72240F2156C40507378CCE3E13F1EE75", 30, 30, {studienGang: Studiengang =>
            fetchAndStoreStudienGangInfo("72240F2156C40507378CCE3E13F1EE75", studienGang)
        }).length must beEqualTo(30)
        StudiengangAttribute.findAll().length must beEqualTo(614)
        Studiengang.findByFach("Alternativer Tourismus").gutachtentLink.get must beEqualTo("http://www.aqas.de/downloads/Gutachten/49_319_BWL")
        Studiengang.findByFach("Alte Geschichte").gutachtentLink must beEqualTo(None)
      }
    }
  }
}