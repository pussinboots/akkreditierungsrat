package org.akkreditierung

import org.akkreditierung.model.{Studiengang, DB}
import org.akkreditierung.AkkreditierungsRatClient._
import scala.concurrent.{Await, Future}
import java.util.concurrent.Executors
import java.util.Date

object AkkreditierungsRatImport extends App {

  //activate ssl debug logging
  //System.setProperty("javax.net.debug", "all")

  DB.WithSSL()
  DB.getMysqlConnection()
  importStudienGaenge()

  def importStudienGaenge(sessionId: String = getSessionId(), step:Int = 100, end: Int = 5100) {

    println(s"Session ${sessionId}")

    //TODO actual the loop has to be defined with a specific end here 5100 studiengang is reached than break need a method to find when the table is at his end
    fetchAndStoreStudienGaenge(sessionId, step, end, {
      studienGang: Studiengang =>
        println(fetchAndStoreStudienGangInfo(sessionId, studienGang))
    })
  }
}

object AkkreditierungsRatUpdate extends App {

  //activate ssl debug logging
  //System.setProperty("javax.net.debug", "all")

  DB.WithSSL()
  DB.getMysqlConnection()
  updateStudiengaenge()

  def updateStudiengaenge(sessionId: String = getSessionId(), threadCount: Int = 4) = {
    println(s"Session ${sessionId}")
    val nextId = { var i = 0; () => { i += 1; i} }

    val studienGaenge = Studiengang.findAllNotUpdatedInLastSevenDays()

    import scala.concurrent.duration._
    import scala.concurrent._
    implicit val ec = new ExecutionContext {
      val threadPool = Executors.newFixedThreadPool(threadCount);
      def execute(runnable: Runnable) = threadPool.submit(runnable)
      def reportFailure(t: Throwable) = t.printStackTrace()
    }

    val tasks: Seq[Future[Studiengang]] = for(studienGang <- studienGaenge) yield future {
        val changed = fetchAndStoreStudienGangInfo(sessionId, studienGang, UpdateStudienGangAttribute)
        if (changed) {
          studienGang.modifiedDate = Some(new Date())
          Studiengang.UpdateModifiedDate(studienGang)
          println(s"Updated ${studienGang}")
          println(nextId())
        }
        Studiengang.UpdateUpdateDate(new Date(), studienGang)
    }

    val aggregated: Future[Seq[Studiengang]] = Future.sequence(tasks)

    val squares: Seq[Studiengang] = Await.result(aggregated, Duration.Inf)
    println("End updating")
    nextId() - 1
  }
  sys.exit()
}
