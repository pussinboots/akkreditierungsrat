package org.akkreditierung

import org.specs2.mutable._
import org.akkreditierung.model._
import org.akkreditierung.AkkreditierungsRatClient._
import org.akkreditierung.test.HSQLDbBefore
import co.freeside.betamax.{MatchRule, TapeMode, Recorder}
import co.freeside.betamax.proxy.jetty.ProxyServer
import scala.Some
import java.util.Comparator
import co.freeside.betamax.message.Request

class AkkreditierungsRatImportSpec extends Specification with HSQLDbBefore {

  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")

  //activate betamax proxy for dispatch

  class NullableBodyMatcher extends Comparator[Request] {
    override def compare(a: Request, b: Request) = {
      if (a.hasBody) {
        val bodyA = scala.io.Source.fromInputStream(a.getBodyAsBinary).mkString
        val bodyB = scala.io.Source.fromInputStream(b.getBodyAsBinary).mkString
        bodyA.compareTo(bodyB)
      } else
        0
    }
  }

  override def initTestData() {
    sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")
    val recorder = new Recorder
    val proxyServer = new ProxyServer(recorder)
    val mode: Option[TapeMode] = Some(TapeMode.READ_ONLY)
    import collection.JavaConversions._
    val list: java.util.List[Comparator[Request]] = Seq(MatchRule.method, MatchRule.uri, new NullableBodyMatcher())
    recorder.insertTape("akkreditierungsratimport", Map("match" -> list))
    recorder.getTape.setMode(mode.getOrElse(recorder.getDefaultMode()))
    proxyServer.start()
    try {
      val sessionId = getSessionId()
      fetchAndStoreStudienGaenge(sessionId, 30, 120, {
        studienGang: Studiengang =>
          fetchAndStoreStudienGangInfo(sessionId, studienGang)
      })
    } finally {
      recorder.ejectTape()
      proxyServer.stop()
    }
  }

  "The AkkreditierungsRat Client" should {
    "fetch studiengaenge from playback" in {
      "fetch and store studiengaenge in the database" in {
        initTestData()
        Job.findLatest().get.newEntries must beEqualTo(239)
        Studiengang.findAll().length must beEqualTo(239)
      }

//      "check that 1226 studiengang attribute are stored in the database for the 30 fetched studiengaenge" in {
//        Job.findLatest().get.newEntries must beEqualTo(60)
//        StudiengangAttribute.findAll().length must beEqualTo(1226)
//        Studiengang.findByFach("Alternativer Tourismus").gutachtentLink.get must beEqualTo("http://www.aqas.de/downloads/Gutachten/49_319_BWL")
//        Studiengang.findByFach("Altertumswissenschaften").gutachtentLink must beEqualTo(None)
//        Studiengang.findByFach("Altertumswissenschaften").jobId must beEqualTo(Job.findLatest().get.id)
//        val source = Source.FindByName("akkreditierungsrat")
//        Studiengang.findByFach("Altertumswissenschaften").sourceId must beEqualTo(source.get.id.get)
//        Studiengang.findByFach("Advanced Physical Methods in Radiotherapy").sourceId must beEqualTo(source.get.id.get)
//      }
    }
  }
}