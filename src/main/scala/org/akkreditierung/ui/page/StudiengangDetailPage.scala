package org.akkreditierung.ui.page

import org.akkreditierung.ui.model._
import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.util.ArrayList
import org.wicket.scala.RepeatingViews._
import org.wicket.scala.Fields._

object StudiengangDetailPage {
  final val PAGE_PARAMETER_ID: String = "studiengangId"
  private final val serialVersionUID: Long = 1L
}

class StudiengangDetailPage(parameters: PageParameters) extends WebPage(parameters) {
    val studiengaengeAttributes = new StudiengaengeAttributeBean().findAll(parameters.get(StudiengangDetailPage.PAGE_PARAMETER_ID).toInt(1))
    val provider = new ListDataProvider[StudiengaengeAttribute](new ArrayList[StudiengaengeAttribute](studiengaengeAttributes.values))
    val d = dataView("displayPanel", provider) {(entry: StudiengaengeAttribute, item: Item[StudiengaengeAttribute]) =>
      item.add(new Label("key_column", entry.getK))
      item.add(labelWithSpecialEscaping("value_column", entry.getV))
    }
    add(d)
}