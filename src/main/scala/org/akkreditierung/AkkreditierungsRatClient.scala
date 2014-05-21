package org.akkreditierung

import dispatch.Defaults.executor
import dispatch.Http
import dispatch.enrichFuture
import dispatch.implyRequestVerbs
import dispatch.url
import org.htmlcleaner.{TagNode, HtmlCleaner}
import org.apache.commons.lang3.StringEscapeUtils
import scala.collection.mutable
import scala.Some
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

object AkkreditierungsRatClient {
  import DB.dal._
  import DB.dal.profile.simple._
  val sourceAkkreditierungsRat = DB.db withDynSession findOrInsert(SourceAkkreditierungsRat.name)

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
    import DB.dal._
    import DB.dal.profile.simple._
    val job = DB.db withDynSession jobInsert(Job(0))

    val neueStudienGaenge = mutable.MutableList[Studiengang]()
    import scala.collection.mutable
    DB.db withDynSession {
      val checkSumMap: mutable.Map[String, Studiengang] = findAllStudienGangs().list().map(elem => elem.checkSum->elem)(collection.breakOut)
      //fetch bechelar studiengänge
      try {
        fetchStudienGaengeByBezugsTyp(start = 0, end, step, sessionId, "3", job, checkSumMap, neueStudienGaenge, block)
        //fetch master studiengänge
        fetchStudienGaengeByBezugsTyp(start = 0, end, step, sessionId, "4", job, checkSumMap, neueStudienGaenge, block)
      } catch {
 	 case e: Exception => e.printStackTrace()
	} finally {
        //updateOrDelete(Job(id = job.id, newEntries = neueStudienGaenge.size, status = "finished"))
      }
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
          DB.db withDynSession {
            val studienGang =  Studiengang(None, Some(job.id), data(0), data(1), data(2), data(3), Option(link),  DateUtil.nowDateTimeOpt(), DateUtil.nowDateTimeOpt(), sourceAkkreditierungsRat.get.id)
            if (!checkSumMap.contains(studienGang.checkSum)) {
              val stored = studienGanginsert(studienGang)
              neueStudienGaenge += stored
              block(stored)
              println(s"insert ${studienGang}")
              checkSumMap.put(studienGang.checkSum, studienGang)
            } else {
              //println(s"already exists ${studienGang}")
            }
          }
        }
      }
    }
  }

  def InsertStudienGangAttribute(studienGang: Studiengang, studiengangAttribute: StudiengangAttribute) = {
    studiengangAttributes.insert(studiengangAttribute)
    false
  }

  def UpdateStudienGangAttribute(studienGang: Studiengang, studiengangAttribute: StudiengangAttribute): Boolean = {
    val attributes = DB.db withDynSession studienGang.attributes
    val st = attributes.get(studiengangAttribute.key)
    if (st == None) {
      InsertStudienGangAttribute(studienGang, studiengangAttribute)
      return true
    } else if (!st.get.equals(studiengangAttribute)) {
      (for {attribute <- studiengangAttributes if attribute.id === studiengangAttribute.id && attribute.key === studiengangAttribute.key} yield (attribute.value)).update(studiengangAttribute.value)
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
            (for {gang <- studiengangs if gang.id === studienGang.id} yield (gang.gutachtentLink)).update(Some(v))
          }
      }
    }
    change
  }

  def classAttributeEquals(elem: TagNode, classToEqual: String): Boolean = {
    elem.getAttributeByName("class") != null && elem.getAttributeByName("class").equalsIgnoreCase(classToEqual)
  }
}
