package org.akkreditierung

import org.akkreditierung.AkkreditierungsRatClient._
import java.util.concurrent.Executors
import scala.slick.session.Database
import Database.threadLocalSession
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick.Studiengang

object AkkreditierungsRatImport extends App {

  DB.WithSSL()
  importStudienGaenge()

  def importStudienGaenge(sessionId: String = getSessionId(), step:Int = 30, end: Int = 5100) {

    println(s"Session ${sessionId}")

    //TODO actual the loop has to be defined with a specific end here 5100 studiengang is reached than break need a method to find when the table is at his end
    fetchAndStoreStudienGaenge(sessionId, step, end, {
      studienGang: Studiengang =>
        fetchAndStoreStudienGangInfo(sessionId, studienGang)
    })
  }
}

object AkkreditierungsRatUpdate extends App {

  import DB.dal._
  import DB.dal.profile.simple._
  //activate ssl debug logging
  //System.setProperty("javax.net.debug", "all")

  DB.WithSSL()
  updateStudiengaenge()

  def updateStudiengaenge(sessionId: String = getSessionId(), threadCount: Int = 4, days: Int = 7) = {
    println(s"Session ${sessionId}")
    val nextId = { var i = 0; () => { i += 1; i} }

    val studienGaenge = DB.db withSession (for {gang <- Studiengangs if gang.updateDate < DateUtil.daysBeforDateTime(days)} yield (gang)).list

    import scala.concurrent.duration._
    import scala.concurrent._
    implicit val ec = new ExecutionContext {
      val threadPool = Executors.newFixedThreadPool(threadCount);
      def execute(runnable: Runnable) = threadPool.submit(runnable)
      def reportFailure(t: Throwable) = t.printStackTrace()
    }

    val tasks: Seq[Future[Studiengang]] = for(studienGang <- studienGaenge) yield future {
        val changed = fetchAndStoreStudienGangInfo(sessionId, studienGang, UpdateStudienGangAttribute)
        val now = DateUtil.nowDateTimeOpt()
        if (changed) {
          DB.db withSession (for {gang <- Studiengangs if gang.id === studienGang.id} yield (gang.modifiedDate)).update(now)
          println(s"Updated ${studienGang.copy(modifiedDate = now)}")
          println(nextId())
        }
        DB.db withSession (for {gang <- Studiengangs if gang.id === studienGang.id} yield (gang.updateDate)).update(now)
        studienGang.copy(modifiedDate = now)
    }

    val aggregated: Future[Seq[Studiengang]] = Future.sequence(tasks)

    val squares: Seq[Studiengang] = Await.result(aggregated, Duration.Inf)
    println("End updating")
    nextId() - 1
  }
  sys.exit()
}
