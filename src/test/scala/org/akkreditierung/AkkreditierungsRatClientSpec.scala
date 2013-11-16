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

class AkkreditierungsRatClientSpec extends Specification with HSQLDbBefore {

  //activate betamax proxy for dispatch
  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")

  override def initTestData() {
    sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")
    val recorder = new Recorder
    val proxyServer = new ProxyServer(recorder)
    val mode: Option[TapeMode] = Some(TapeMode.READ_ONLY)
    import collection.JavaConversions._
    val list: java.util.List[Comparator[Request]] = Seq(MatchRule.method, MatchRule.uri, new NullableBodyMatcher())
    recorder.insertTape("akkreditierungsratclient", Map("match" -> list))
    recorder.getTape.setMode(mode.getOrElse(recorder.getDefaultMode()))
    proxyServer.start()
    try {
      val sessionId = "B787EA9B6B8633E4DC39904A77BC2F79"
      fetchAndStoreStudienGaenge(sessionId, 30, 30, {
        studienGang: Studiengang =>
          fetchAndStoreStudienGangInfo(sessionId, studienGang)
      })
    } finally {
      recorder.ejectTape()
      proxyServer.stop()
    }
  }

  "The AkkreditierungsRat Client" should {
    "fetch and store studiengaenge in the database" in {
      Job.findLatest().get.newEntries must beEqualTo(60)
      Studiengang.findAll().length must beEqualTo(60)
    }

    "check that 1226 studiengang attribute are stored in the database for the 30 fetched studiengaenge" in {
      Job.findLatest().get.newEntries must beEqualTo(60)
      StudiengangAttribute.findAll().length must beEqualTo(1239)
      Studiengang.findByFach("Alternativer Tourismus").gutachtentLink.get must beEqualTo("http://www.aqas.de/downloads/Gutachten/49_319_BWL")
      Studiengang.findByFach("Altertumswissenschaften").gutachtentLink must beEqualTo(None)
      Studiengang.findByFach("Altertumswissenschaften").jobId must beEqualTo(Job.findLatest().get.id)
      val source = Source.FindOrCreateSourceAkkreditierungsrat()
      Studiengang.findByFach("Altertumswissenschaften").sourceId must beEqualTo(source.get.id.get)
      Studiengang.findByFach("Advanced Physical Methods in Radiotherapy").sourceId must beEqualTo(source.get.id.get)
    }

    "retrieve sessionid" in Betamax("akkreditierungsratsession") {
      getSessionId() must beEqualTo("6D6DBF92C6C5F6C180E1A41333A92D1F")
    }
  }
}