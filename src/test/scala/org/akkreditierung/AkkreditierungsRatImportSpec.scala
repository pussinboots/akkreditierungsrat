package org.akkreditierung

import org.specs2.mutable._
import org.akkreditierung.model._
import org.akkreditierung.AkkreditierungsRatClient._
import org.akkreditierung.test.{NullableBodyMatcher, Betamax, HSQLDbBefore}
import co.freeside.betamax.{MatchRule, TapeMode, Recorder}
import co.freeside.betamax.proxy.jetty.ProxyServer
import scala.Some
import java.util.Comparator
import co.freeside.betamax.message.Request

class AkkreditierungsRatImportSpec extends Specification with HSQLDbBefore {

  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")

  //activate betamax proxy for dispatch

  "The AkkreditierungsRatImport Client" should {
    "fetch studiengaenge from akkreditierungs database" in {
      "fetch and store new studiengaenge in the database" in Betamax(tape="akkreditierungsratimport", list=Seq(MatchRule.method, MatchRule.uri, new NullableBodyMatcher())) {
        AkkreditierungsRatImport.importStudienGaenge("2B2C176F125F48AF828FE2A8923FC461", 30, 120)
        Job.findLatest().get.newEntries must beEqualTo(239)
        Studiengang.findAll().length must beEqualTo(239)
      }


      //TODO how to check update perform well
      "update existing studiengaenge in the database" in Betamax(tape="akkreditierungsratimport", list=Seq(MatchRule.method, MatchRule.uri, new NullableBodyMatcher())) {
        AkkreditierungsRatImport.importStudienGaenge("2B2C176F125F48AF828FE2A8923FC461", 30, 120)
        AkkreditierungsRatUpdate.updateStudiengaenge("2B2C176F125F48AF828FE2A8923FC461", 1)
        Job.findLatest().get.newEntries must beEqualTo(239)
        Studiengang.findAll().length must beEqualTo(239)
      }
    }
  }
}