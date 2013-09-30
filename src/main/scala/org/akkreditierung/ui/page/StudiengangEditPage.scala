package org.akkreditierung.ui.page

import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.repeater.data.DataView
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.request.mapper.parameter.PageParameters
import scala.collection.JavaConversions._
import org.apache.wicket.markup.html.form.{TextArea, Form, TextField}
import org.apache.wicket.model.{CompoundPropertyModel, PropertyModel, Model}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.akkreditierung.ui.model.{AdvertiserConfig, StudiengaengeAttribute}
import org.akkreditierung.model.{Studiengang, StudiengangAttribute}

object StudiengangEditPage {
  final val PAGE_PARAMETER_ID: String = "studiengangId"
  private final val fields = List("Abschlussgrad", "Akkreditiert", "Akkreditiert bis", "Auflagen", "Auflagen erfüllt", "Besondere Studienform", "E-mail", "Erstakkreditierung", "Fakultät / Fachbereich", "Fax", "Gutachten Link", "Hochschule", "Kontaktperson", "Mitglieder der Gutachtergruppe", "Profil des Studiengangs", "Regelstudienzeit", "Studienfach", "Telefon","von", "Weitere Informationen", "www", "Zusammenfassende Bewertung")
  private final val serialVersionUID: Long = 1L
}

class StudiengangEditPage(parameters: PageParameters) extends WebPage(parameters) {
  val provider = new ListDataProvider[String](StudiengangEditPage.fields)
  val list = StudiengangEditPage.fields map {field=>
    val s = new StudiengaengeAttribute()
    s.k = field
    s
  }
  val studienGang = new AdvertiserConfig()
  val fach = new TextField[String]("fach");
  val abschluss = new TextField[String]("abschluss");
  val hochschule = new TextField[String]("hochschule");
  val bezugstyp = new TextField[String]("bezugstyp");
  val gutachtenLink = new TextField[String]("gutachtenLink");
  val form = new Form("form", new CompoundPropertyModel[AdvertiserConfig](studienGang)) {
    override def  onSubmit() {
      println(studienGang)
      val storedStudienGang = Studiengang.Insert(studienGang.toStudienGang())
      list filter(entry => entry.v != null) foreach {entry =>
          println(s"${entry.k} = ${entry.v}")
          StudiengangAttribute.Insert(StudiengangAttribute(storedStudienGang.id.get, entry.k, entry.v))
      }
      clearInput()
    }
  }

  val dataView = new ListView[StudiengaengeAttribute]("displayPanel", list) {
    protected def populateItem(item: ListItem[StudiengaengeAttribute]) {
      val entry = item.getModelObject
      item.add(new Label("key_column", entry.k))
      item.add(new TextArea[String]("value_column", new PropertyModel[String](entry, "v")))
    }
  }
  form.add(dataView)
  form.add(fach)
  form.add(abschluss)
  form.add(hochschule)
  form.add(bezugstyp)
  form.add(gutachtenLink)
  add(form)
}