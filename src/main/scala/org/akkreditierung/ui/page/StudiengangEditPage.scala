package org.akkreditierung.ui.page

import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.request.mapper.parameter.PageParameters
import scala.collection.JavaConversions._
import org.apache.wicket.markup.html.form.{TextArea, Form, TextField}
import org.apache.wicket.model.{CompoundPropertyModel, PropertyModel}
import org.apache.wicket.markup.html.list.ListItem
import org.wicket.scala.RepeatingViews._
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick._
import org.apache.wicket.markup.html.panel.FeedbackPanel
import scala.slick.session.Database.threadLocalSession

object StudiengangEditPage {
  final val PAGE_PARAMETER_ID: String = "studiengangId"
  private final val fields = List("Abschlussgrad", "Akkreditiert", "Akkreditiert bis", "Auflagen", "Auflagen erfüllt", "Besondere Studienform", "E-mail", "Erstakkreditierung", "Fakultät / Fachbereich", "Fax", "Gutachten Link", "Hochschule", "Kontaktperson", "Mitglieder der Gutachtergruppe", "Profil des Studiengangs", "Regelstudienzeit", "Studienfach", "Telefon","von", "Weitere Informationen", "www", "Zusammenfassende Bewertung")
  private final val serialVersionUID: Long = 1L
}

@AuthorizeInstantiation(value=Array("ADMIN"))
class StudiengangEditPage(parameters: PageParameters) extends WebPage(parameters) {
  import org.wicket.scala.WicketDSL._
  val provider = new ListDataProvider[String](StudiengangEditPage.fields)
  val list = StudiengangEditPage.fields map(field=>StudiengangAttributeC.neu(field))
  val studienGang = StudiengangC.neu
  val fach = "fach".textField
  val abschluss = "abschluss".textField
  val hochschule = "hochschule".textField
  val bezugstyp = "bezugstyp".textField
  val gutachtenLink = "gutachtenLink".textField
  val form = new Form("form", new CompoundPropertyModel[Studiengang](studienGang)) {
    override def onSubmit() {
      println(studienGang)
      import DB.dal._
      import DB.dal.profile.simple._
      val storedStudienGang = DB.db withSession Studiengangs.insert(getModelObject)
      val l:Seq[StudiengangAttribute] = list.filter(_.value != null).map(_.copy(id=storedStudienGang.id.get))
      DB.db withSession StudiengangAttributes.insertAll(l:_*)
      clearInput()    //todo not working at the moment clear all input and model values
    }
  }
  val dataView = listView("displayPanel", list) {(entry: StudiengangAttribute, item: ListItem[StudiengangAttribute]) =>
    item.add("key_column".label(entry.key))
    item.add("value_column".textArea(entry, "value"))
  }
  form.add(dataView)
  form.add(fach)
  form.add(abschluss)
  form.add(hochschule)
  form.add(bezugstyp)
  form.add(gutachtenLink)
  form.add(new FeedbackPanel("feedback"))
  add(form)
}