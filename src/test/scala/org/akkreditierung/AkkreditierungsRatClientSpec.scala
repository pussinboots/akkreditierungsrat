package org.akkreditierung

import org.specs2.mutable._
import org.akkreditierung.AkkreditierungsRatClient._
import org.akkreditierung.test.{SlickDbBefore, NullableBodyMatcher, Betamax}
import co.freeside.betamax.{MatchRule, TapeMode, Recorder}
import co.freeside.betamax.proxy.jetty.ProxyServer
import scala.Some
import java.util.Comparator
import co.freeside.betamax.message.Request
import org.akkreditierung.model.slick.{SourceAkkreditierungsRat, Studiengang}
import org.akkreditierung.model.DB
import scala.slick.session.Database
import Database.threadLocalSession

class AkkreditierungsRatClientSpec extends Specification with SlickDbBefore {
  import DB.dal._
  import DB.dal.profile.simple._
  //activate betamax proxy for dispatch
  sys.props.+=("com.ning.http.client.AsyncHttpClientConfig.useProxyProperties" -> "true")

  override def initTestData(db: Database) {
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
      DB.db withSession{
        Jobs.findLatest().get.newEntries must beEqualTo(60)
        Studiengangs.findAll().list.length must beEqualTo(60)
      }
    }

    "check that 1226 studiengang attribute are stored in the database for the 30 fetched studiengaenge" in {
      DB.db withSession{
        Jobs.findLatest().get.newEntries must beEqualTo(60)
        StudiengangAttributes.findAll().list.length must beEqualTo(1239)
        Studiengangs.findByFach("Alternativer Tourismus").first.gutachtenLink.get must beEqualTo("http://www.aqas.de/downloads/Gutachten/49_319_BWL")
        Studiengangs.findByFach("Altertumswissenschaften").first.gutachtenLink must beEqualTo(None)
        Studiengangs.findByFach("Altertumswissenschaften").first.jobId.get must beEqualTo(Jobs.findLatest().get.id)
        val source = Sources.findOrInsert(SourceAkkreditierungsRat.name)
        Studiengangs.findByFach("Altertumswissenschaften").first.sourceId must beEqualTo(source.get.id)
        Studiengangs.findByFach("Advanced Physical Methods in Radiotherapy").first.sourceId must beEqualTo(source.get.id)
      }
    }

    "retrieve sessionid" in Betamax("akkreditierungsratsession") {
      getSessionId() must beEqualTo("6D6DBF92C6C5F6C180E1A41333A92D1F")
    }
  }
}