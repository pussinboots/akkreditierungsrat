package org.akkreditierung

import org.specs2.mutable._
import org.akkreditierung.model._
import org.akkreditierung.test.{NullableBodyMatcher, Betamax, HSQLDbBefore}
import co.freeside.betamax.{MatchRule, TapeMode}
import scala.Some

class AkkreditierungsRatImportSpec extends Specification with HSQLDbBefore {

  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")
  sequential
  //activate betamax proxy for dispatch

  "The AkkreditierungsRatImport Client" should {
    "fetch studiengaenge from akkreditierungs database" in {
      "fetch and store new studiengaenge in the database" in Betamax(tape="akkreditierungsratimport", list=Seq(MatchRule.method, MatchRule.uri, new NullableBodyMatcher())) {
        AkkreditierungsRatImport.importStudienGaenge("2B2C176F125F48AF828FE2A8923FC461", 30, 120)
        Job.findLatest().get.newEntries must beEqualTo(239)
        Studiengang.findAll().length must beEqualTo(239)
      }
    }
  }
//  "The AkkreditierungsRatUpdate Client" should {
//      //TODO how to check update perform well
//      "update existing studiengaenge in the database" in Betamax(tape="akkreditierungsratupdate", list=Seq(MatchRule.method, MatchRule.uri, new NullableBodyMatcher())) {
//        AkkreditierungsRatImport.importStudienGaenge("2B2C176F125F48AF828FE2A8923FC461", 30, 30)
//        "update existing studiengaenge in the database" in {
//          AkkreditierungsRatUpdate.updateStudiengaenge("2B2C176F125F48AF828FE2A8923FC461", 1) must beEqualTo(0)
//          Job.findLatest().get.newEntries must beEqualTo(60)
//          Studiengang.findAll().length must beEqualTo(60)
//        }
//        Job.findLatest().get.newEntries must beEqualTo(60)
//        Studiengang.findAll().length must beEqualTo(60)
//      }
//    }
}