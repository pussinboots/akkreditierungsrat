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

  def fetchAndStoreStudienGaenge(sessionId: String, step: Int= 30, end: Int = 30, block: Studiengang => _) = {
    val checkSumMap: Map[String, Studiengang] = Studiengang.findAll().map(elem => elem.checkSum -> elem)(collection.breakOut)

    val neueStudienGaenge = scala.collection.mutable.MutableList[Studiengang]()
    for (offset <- Range.apply(0, end, step)) {
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
    //TODO kodierungs probleme beim auslesen oder speichern der studien gangs attribute
    val data = childs
      .filter(elem => elem.getAttributeByName("class") != null && elem.getAttributeByName("class").equalsIgnoreCase("ergfeld"))
      .map(elem => StringEscapeUtils.unescapeHtml4(elem.getText().toString))
    if (!data.isEmpty) {
      val map = (keys zip data)(collection.breakOut)
      map foreach {
        case (k, v) =>
          StudiengangAttribute.Insert(StudiengangAttribute(studienGang.id.get, k, v))
          if (k == "Gutachten Link") {
            studienGang.gutachtentLink = Some(v)
            Studiengang.UpdateGutachtentLink(studienGang)
          }
      }
    }
  }
}

object AkkreditierungsRatImport extends App {

  DB.getMysqlConnection()

  val sessionId = "5791E97BB670630389049E710496B606" //TODO get a valid session id automaticly
  println(s"Session ${sessionId}")

  val neueStudienGaenge = fetchAndStoreStudienGaenge(sessionId, 30, 5000, {
    studienGang: Studiengang =>
      println(fetchAndStoreStudienGangInfo(sessionId, studienGang))
  })
}
