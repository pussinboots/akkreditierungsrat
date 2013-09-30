package org.akkreditierung.ui.page

import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.repeater.data.DataView
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.request.mapper.parameter.PageParameters
import scala.collection.JavaConversions._
import org.apache.wicket.markup.html.form.{Form, TextField}
import org.apache.wicket.model.{PropertyModel, Model}
import org.apache.wicket.markup.html.list.{ListItem, ListView}
import org.akkreditierung.ui.model.StudiengaengeAttribute

object StudiengangEditPage {
  final val PAGE_PARAMETER_ID: String = "studiengangId"
  private final val fields = List("von", "Gutachten Link", "www")
  private final val serialVersionUID: Long = 1L
}

class StudiengangEditPage(parameters: PageParameters) extends WebPage(parameters) {
  val provider = new ListDataProvider[String](StudiengangEditPage.fields)
  val s = new StudiengaengeAttribute()
  val list = StudiengangEditPage.fields map {field=>
    val s = new StudiengaengeAttribute()
    s.k = field
    s
  }

  val form = new Form("form", new Model()) {
    override def  onSubmit() {
      list foreach {entry =>
        println(s"${entry.k} = ${entry.v}")
      }
      clearInput()
    }
  }

  val dataView = new ListView[StudiengaengeAttribute]("displayPanel", list) {
    protected def populateItem(item: ListItem[StudiengaengeAttribute]) {
      val entry = item.getModelObject
      item.add(new Label("key_column", entry.k))
      item.add(new TextField("value_column", new PropertyModel[String](entry, "v")))
    }
  }
  form.add(dataView)
  add(form)
}