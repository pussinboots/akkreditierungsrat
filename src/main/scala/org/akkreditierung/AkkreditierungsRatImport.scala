package org.akkreditierung

import dispatch.Defaults.executor
import dispatch.Http
import dispatch.enrichFuture
import dispatch.implyRequestVerbs
import dispatch.url
import org.akkreditierung.model.{DB, StudiengangAttribute, Studiengang}
import AkkreditierungsRatClient._
import org.htmlcleaner.HtmlCleaner
import org.apache.commons.lang3.StringEscapeUtils
import java.sql.Connection

object AkkreditierungsRatClient {
  def getResult(sessionId: String, offset: String = "0") = {
    val post = Map("tid" -> "80520", "reuseresult" -> "false", "stylesheet" -> "tabelle_html_akkr.xsl", "Bezugstyp" -> "3", "sort" -> "2", "offset" -> offset, "maxoffset" -> "", "contenttype" -> "")
    val uri = url("http://www.hs-kompass2.de/kompass/servlet")
    val header = Map("Content-Type" -> "application/x-www-form-urlencoded", "Cookie" -> s"JSESSIONID=${sessionId}")
    val response = Http(uri / "SuperXmlTabelle" << post <:< header)
    response().getResponseBody
  }

  def getStudienGangInfo(sessionId: String, link: String) = {
    val uri = url(link.replace("##sessionId##", sessionId))
    val response = Http(uri)
    val body = response()
    body.getResponseBody()
  }

  def fetchAndStoreStudienGaenge(sessionId: String, end: Int = 30, block: Studiengang => _) = {
    val checkSumMap: Map[String, Studiengang] = Studiengang.findAll().map(elem => elem.checkSum -> elem)(collection.breakOut)

    val neueStudienGaenge = scala.collection.mutable.MutableList[Studiengang]()
    for (offset <- Range.apply(0, end, 30)) {
      val response = getResult(sessionId, s"${offset}")
      val cleaner = new HtmlCleaner
      val props = cleaner.getProperties
      val rootNode = cleaner.clean(response)
      //TODO simplify parsing the html data out into the Studiengang pojo
      val elements = rootNode.getElementsByName("tr", true)
      for (elem <- elements) {
        val childs = elem.getElementsByName("td", false)
        val data = childs
          .filter(elem => elem.getAttributeByName("class") != null && elem.getAttributeByName("class").equalsIgnoreCase("ergfeld"))
          .map(elem => StringEscapeUtils.unescapeHtml4(elem.getText().toString))
        if (!data.isEmpty) {
          val link = childs
            .find(elem => !elem.getElementsByAttValue("title", "Weiter", true, false).isEmpty)
            .get.getElementsByAttValue("title", "Weiter", true, false)(0).getAttributeByName("href")
            .replace("..", "http://www.hs-kompass2.de/kompass")
            .replace(sessionId, "##sessionId##")
          println(s"link ${link}")

          val studienGang = Studiengang(None, data(0), data(1), data(2), data(3), link)
          if (!checkSumMap.contains(studienGang.checkSum)) {
            Studiengang.Insert(studienGang)
            neueStudienGaenge += studienGang
            block(studienGang)
            println(s"insert ${studienGang}")
          } else {
            println(s"already exists ${studienGang}")
          }
        }
      }
    }
    neueStudienGaenge
  }

  def fetchAndStoreStudienGangInfo(sessionId: String, studienGang: Studiengang) {
    val response = getStudienGangInfo(sessionId, studienGang.link)
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    val rootNode = cleaner.clean(response)
    //TODO simplify parsing the html data out into the Studiengang pojo
    val childs = rootNode.getElementsByName("td", true)
    val keys = childs
      .filter(elem => elem.getAttributeByName("class") != null && elem.getAttributeByName("class").equalsIgnoreCase("ergspalte"))
      .map(elem => StringEscapeUtils.unescapeHtml4(elem.getText().toString))
    val data = childs
      .filter(elem => elem.getAttributeByName("class") != null && elem.getAttributeByName("class").equalsIgnoreCase("ergfeld"))
      .map(elem => StringEscapeUtils.unescapeHtml4(elem.getText().toString))
    if (!data.isEmpty) {
      val map = (keys zip data)(collection.breakOut)
      println(s"link ${map}")
      map foreach {
        case (k, v) =>
          StudiengangAttribute.Insert(StudiengangAttribute(studienGang.id.get, k, v))
      }
    }
  }
}

object AkkreditierungsRatImport extends App {

  DB.getMysqlConnection(None)
  //MySQL.createTableStudienGang()

  val sessionId = "B053119144473C5A303894A328B7B42C" //TODO get a valid session id automaticly
  println(s"Session ${sessionId}")

  val neueStudienGaenge = fetchAndStoreStudienGaenge(sessionId, 30, {
    studienGang: Studiengang =>
      println(fetchAndStoreStudienGangInfo(sessionId, studienGang))
  })

  //val allStudienGaenge = Studiengang.findAll()
  //neueStudienGaenge.foreach {
  //  studienGang =>
  //    println(fetchAndStoreStudienGangInfo(sessionId, studienGang))
  //}

  /*def getSessionId() = {
   val post = Map("tid" -> "80520",
     "kennung" -> "akkr",
     "password" -> "anfang12",
     "sort" -> "2",
     "Bezugstyp" -> "3",
     "Fach" -> "",
     "Hochschulort" -> "",
     "Hochschultyp" -> "",
     "Bundesland" -> "",
     "Studienform" -> "",
     "Button1" -> "Studieng%E4nge+anzeigen")

   //"Cookie" ->"JSESSIONID=06B3F4C4704A2AC0B8A0F0CB54A22FAD")
   val h = (headers + ("User-Agent" -> "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/28.0.1500.71 Chrome/28.0.1500.71 Safari/537.36"))
   val response = Http(uri / "SuperXmlTabelle" << post <:< h)
   val body = response()
   body.getHeader("Set-Cookie").replace("; Path=/kompass", "")
 } */
}
