package org.akkreditierung

import org.specs2.mutable._
import org.akkreditierung.model._
import org.akkreditierung.test.{HSQLDbBefore, NullableBodyMatcher, Betamax}
import co.freeside.betamax.MatchRule._

class AkkreditierungsRatImportSpec extends Specification with HSQLDbBefore {

  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")
  //activate betamax proxy for dispatch
  sequential
  isolated //to have seperated hsql databases

  def matchStudienGangAttribute(fach: String, attribute: String, expectedValue: String) = {
    Studiengang.findByFach(fach)
      .studiengangAttributes.get(attribute).get.value must beEqualTo(expectedValue)
  }

  "The AkkreditierungsRatUpdate Client" should {
      //TODO how to check update perform well
      "update existing studiengaenge in the database" in Betamax(tape="akkreditierungsratupdate", list=Seq(method, uri, new NullableBodyMatcher())) {
        AkkreditierungsRatImport.importStudienGaenge("276F4168360E2A5242E22680D3BBBCF0", step=30, end=30)
        Job.findLatest().get.newEntries must beEqualTo(60)
        Studiengang.findAll().length must beEqualTo(60)
        matchStudienGangAttribute("Agricultural Science and Resource Management in the Tropics and Subtropics (ARTS)", "Profil des Studiengangs" , "Siehe Gutachten")
        val updatedStudienGaenge = AkkreditierungsRatUpdate.updateStudiengaenge("C73516D8FB3CB7A599399E5E0E464C42", threadCount=4)
        updatedStudienGaenge must beEqualTo(2)
        matchStudienGangAttribute("Agricultural Science and Resource Management in the Tropics and Subtropics (ARTS)", "Profil des Studiengangs" , "Siehe Akkreditierungs-Gutachten")
        matchStudienGangAttribute("Agricultural and Food Economics (AFECO)", "Hochschule 2" , "Rheinische Friedrich-Wilhelms-Universität Bonn")
      }
    }
}