package org.akkreditierung

import dispatch.Defaults.executor
import dispatch.Http
import dispatch.enrichFuture
import dispatch.implyRequestVerbs
import dispatch.url
import org.akkreditierung.model._
import AkkreditierungsRatClient._
import org.htmlcleaner.{TagNode, HtmlCleaner}
import org.apache.commons.lang3.StringEscapeUtils
import scala.Some
import scala.collection.mutable
import scala.concurrent._
import scala.Some
import java.util.Date

object AkkreditierungsRatClient {

  val sourceAkkreditierungsRat = Source.FindOrCreateSourceAkkreditierungsrat()

  def getResult(sessionId: String, offset: String = "0", bezugsTyp: String = "3", maxOffset: String = "30") = {
    val post = Map("tid" -> "80520", "reuseresult" -> "false", "stylesheet" -> "tabelle_html_akkr.xsl", "Bezugstyp" -> bezugsTyp, "sort" -> "2", "offset" -> offset, "maxoffset" -> maxOffset, "contenttype" -> "")
    val uri = url("http://www.hs-kompass2.de/kompass/servlet")
    val header = Map("Content-Type" -> "application/x-www-form-urlencoded", "Cookie" -> s"JSESSIONID=${sessionId}")
    val response = Http(uri / "SuperXmlTabelle" << post <:< header)
    response().getResponseBody
  }

  def getSessionId() = {
    val post = Map("tid" -> "80520", "kennung" -> "akkr", "passwort" -> "anfang12", "sort" -> "2", "Bezugstyp" -> "3")
    val uri = url("http://www.hs-kompass2.de/kompass/servlet")
    val header = Map("Content-Type" -> "application/x-www-form-urlencoded", "Referer" -> "http://www.hs-kompass2.de/kompass/xml/akkr/maske.html")
    val response = Http(uri / "SuperXmlTabelle" << post <:< header)
    response().getHeader("Set-Cookie").substring(11, 43)
  }

  def getStudienGangInfo(sessionId: String, link: String) = {
    val uri = url(link.replace("##sessionId##", sessionId))
    val response = Http(uri)
    val body = response()
    body.getResponseBody()
  }

  def fetchAndStoreStudienGaenge(sessionId: String, step: Int = 30, end: Int = 30, block: Studiengang => (Unit)) = {
    val job = Job.Insert(Job())

    val neueStudienGaenge = mutable.MutableList[Studiengang]()
    val checkSumMap: mutable.Map[String, Studiengang] = Studiengang.findAll().map(elem => elem.checkSum -> elem)(collection.breakOut)
    //fetch bechelar studiengänge
    try {
      fetchStudienGaengeByBezugsTyp(start = 0, end, step, sessionId, "3", job, checkSumMap, neueStudienGaenge, block)
      //fetch master studiengänge
      fetchStudienGaengeByBezugsTyp(start = 0, end, step, sessionId, "4", job, checkSumMap, neueStudienGaenge, block)
    } finally {
      Job.UpdateOrDelete(Job(id = job.id, newEntries = neueStudienGaenge.size, status = "finished"))
    }
    neueStudienGaenge
  }


  def fetchStudienGaengeByBezugsTyp(start: Int = 0, end: Int, step: Int, sessionId: String, bezugsTyp: String, job: Job, checkSumMap: mutable.Map[String, Studiengang], neueStudienGaenge: mutable.MutableList[Studiengang], block: (Studiengang) => (Unit)) {
    for (offset <- Range.apply(start, end, step)) {
      val response = getResult(sessionId, s"${offset}", bezugsTyp, maxOffset = step.toString)
      val cleaner = new HtmlCleaner
      val props = cleaner.getProperties
      val rootNode = cleaner.clean(response)
      //TODO simplify parsing the html data out into the Studiengang pojo
      val elements = rootNode.getElementsByName("tr", true)
      for (elem <- elements) {
        val childs = elem.getElementsByName("td", false)
        val data = childs
          .filter(elem => classAttributeEquals(elem, "ergfeld"))
          .map(elem => StringEscapeUtils.unescapeHtml4(elem.getText().toString))
        if (!data.isEmpty) {
          val link = childs
            .find(elem => !elem.getElementsByAttValue("title", "Weiter", true, false).isEmpty)
            .get.getElementsByAttValue("title", "Weiter", true, false)(0).getAttributeByName("href")
            .replace("..", "http://www.hs-kompass2.de/kompass")
            .replace(sessionId, "##sessionId##")

          val studienGang = Studiengang(None, job.id, data(0), data(1), data(2), data(3), Option(link), modifiedDate = Some(new Date()), sourceId = sourceAkkreditierungsRat.get.id.get)
          if (!checkSumMap.contains(studienGang.checkSum)) {
            Studiengang.Insert(studienGang)
            neueStudienGaenge += studienGang
            block(studienGang)
            println(s"insert ${studienGang}")
            checkSumMap.put(studienGang.checkSum, studienGang)
          } else {
            //println(s"already exists ${studienGang}")
          }
        }
      }
    }
  }

  def InsertStudienGangAttribute(studienGang: Studiengang, studiengangAttribute: StudiengangAttribute) = {
    StudiengangAttribute.Insert(studiengangAttribute)
    false
  }

  def UpdateStudienGangAttribute(studienGang: Studiengang, studiengangAttribute: StudiengangAttribute): Boolean = {
    val attributes = studienGang.studiengangAttributes
    val st = attributes.get(studiengangAttribute.key)
    if (st == None) {
      InsertStudienGangAttribute(studienGang, studiengangAttribute)
      return true
    } else if (!st.get.equals(studiengangAttribute)) {
      StudiengangAttribute.Update(studiengangAttribute)
      return true
    }
    return false
  }

  def fetchAndStoreStudienGangInfo(sessionId: String, studienGang: Studiengang, persit: (Studiengang, StudiengangAttribute) => (Boolean) = InsertStudienGangAttribute) = {
    val response = getStudienGangInfo(sessionId, studienGang.link.get)
    var change = false
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    props.setTransSpecialEntitiesToNCR(true)
    val rootNode = cleaner.clean(response)
    //TODO simplify parsing the html data out into the Studiengang pojo
    val childs = rootNode.getElementsByName("td", true)
    val keys = childs
      .filter(elem => classAttributeEquals(elem, "ergspalte"))
      .map(elem => StringEscapeUtils.unescapeHtml4(elem.getText().toString))
    val data = childs
      .filter(elem => classAttributeEquals(elem, "ergfeld"))
      .map(elem => StringEscapeUtils.unescapeHtml4(elem.getText().toString))
    if (!data.isEmpty) {
      val map = (keys zip data)(collection.breakOut)
      map foreach {
        case (k, v) =>
          change = change || persit(studienGang, StudiengangAttribute(studienGang.id.get, k, v))
          if (k == "Gutachten Link") {
            studienGang.gutachtentLink = Some(v)
            Studiengang.UpdateGutachtentLink(studienGang)
          }
      }
    }
    change
  }

  def classAttributeEquals(elem: TagNode, classToEqual: String): Boolean = {
    elem.getAttributeByName("class") != null && elem.getAttributeByName("class").equalsIgnoreCase(classToEqual)
  }
}
