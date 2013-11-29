package org.akkreditierung

import org.specs2.mutable._
import org.akkreditierung.test.{SlickDbBefore, NullableBodyMatcher, Betamax}
import co.freeside.betamax.MatchRule._
import org.akkreditierung.model.DB
import DB.dal._
import DB.dal.profile.simple._
import scala.slick.session.Database
import Database.threadLocalSession
class AkkreditierungsRatImportSpec extends Specification with SlickDbBefore {

  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")
  //activate betamax proxy for dispatch
  sequential
  isolated //to have seperated hsql databases

  def matchStudienGangAttribute(fach: String, attribute: String, expectedValue: String) = {
    DB.db withSession Studiengangs.findByFach(fach).first.attributes.get(attribute).get.value must beEqualTo(expectedValue)
  }

  "The AkkreditierungsRatUpdate Client" should {
      //TODO how to check update perform well
      "update existing studiengaenge in the database" in Betamax(tape="akkreditierungsratupdate", list=Seq(method, uri, new NullableBodyMatcher())) {
        DB.db withSession{
          AkkreditierungsRatImport.importStudienGaenge("276F4168360E2A5242E22680D3BBBCF0", step=30, end=30)
          Jobs.findLatest().get.newEntries must beEqualTo(60)
          Studiengangs.findAll().list.length must beEqualTo(60)
          matchStudienGangAttribute("Agricultural Science and Resource Management in the Tropics and Subtropics (ARTS)", "Profil des Studiengangs" , "Siehe Gutachten")
          val updatedStudienGaenge = AkkreditierungsRatUpdate.updateStudiengaenge("C73516D8FB3CB7A599399E5E0E464C42", threadCount=4, days=0)
          updatedStudienGaenge must beEqualTo(2)
          matchStudienGangAttribute("Agricultural Science and Resource Management in the Tropics and Subtropics (ARTS)", "Profil des Studiengangs" , "Siehe Akkreditierungs-Gutachten")
          matchStudienGangAttribute("Agricultural and Food Economics (AFECO)", "Hochschule 2" , "Rheinische Friedrich-Wilhelms-Universität Bonn")
        }
      }
    }
}