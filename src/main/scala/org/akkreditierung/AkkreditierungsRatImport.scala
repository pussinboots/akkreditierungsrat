package org.akkreditierung

import org.akkreditierung.model.{Studiengang, DB}
import org.akkreditierung.AkkreditierungsRatClient._

object AkkreditierungsRatImport extends App {

  //activate ssl debug logging
  //System.setProperty("javax.net.debug", "all")

  DB.WithSSL()
  DB.getMysqlConnection()

  val sessionId = getSessionId()
  println(s"Session ${sessionId}")

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

  val studienGaenge = Studiengang.findAll()

  for(studienGang <- studienGaenge) {
      println(fetchAndStoreStudienGangInfo(sessionId, studienGang, UpdateStudienGangAttribute))
  }
}
