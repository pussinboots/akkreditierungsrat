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

  val sessionId = getSessionId()
  println(s"Session ${sessionId}")

  //TODO actual the loop has to be defined with a specific end here 5100 studiengang is reached than break need a method to find when the table is at his end
  fetchAndStoreStudienGaenge(sessionId, 100, 5100, {
    studienGang: Studiengang =>
      println(fetchAndStoreStudienGangInfo(sessionId, studienGang))
  })
}

object AkkreditierungsRatUpdate extends App {

  //activate ssl debug logging
  //System.setProperty("javax.net.debug", "all")

  DB.WithSSL()
  DB.getMysqlConnection()

  val sessionId = getSessionId()
  println(s"Session ${sessionId}")

  val studienGaenge = Studiengang.findAllNotUpdatedInLastSevenDays()

  import scala.concurrent.duration._
  import scala.concurrent._
  implicit val ec = new ExecutionContext {
    val threadPool = Executors.newFixedThreadPool(4);
    def execute(runnable: Runnable) = threadPool.submit(runnable)
    def reportFailure(t: Throwable) = t.printStackTrace()
  }

  val tasks: Seq[Future[Studiengang]] = for(studienGang <- studienGaenge) yield future {
      val changed = fetchAndStoreStudienGangInfo(sessionId, studienGang, UpdateStudienGangAttribute)
      if (changed) {
        studienGang.modifiedDate = Some(new Date())
        Studiengang.UpdateModifiedDate(studienGang)
        println(s"Updated ${studienGang}")
      }
      Studiengang.UpdateUpdateDate(new Date(), studienGang)
  }

  val aggregated: Future[Seq[Studiengang]] = Future.sequence(tasks)

  val squares: Seq[Studiengang] = Await.result(aggregated, Duration.Inf)
  println("End updating")
  sys.exit()
}
