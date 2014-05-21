package org.akkreditierung.ui.page

import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.wicket.scala.RepeatingViews._
import org.wicket.scala.Fields._
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import collection.JavaConversions._

object StudiengangDetailPage {
  final val PAGE_PARAMETER_ID: String = "studiengangId"
  private final val serialVersionUID: Long = 1L
}

class StudiengangDetailPage(parameters: PageParameters) extends WebPage(parameters) {
  import DB.dal.profile.simple._
  val studiengaengeAttributes =  DB.db withDynSession DB.dal.studiengangAttributes.filter(_.id === getStudiengangId).list
  val provider = new ListDataProvider[StudiengangAttribute](studiengaengeAttributes)
  val d = dataView("displayPanel", provider) {(entry: StudiengangAttribute, item: Item[StudiengangAttribute]) =>
    item.add(new Label("key_column", entry.key))
    item.add(labelWithSpecialEscaping("value_column", entry.value))
  }
  add(d)

  def getStudiengangId = parameters.get(StudiengangDetailPage.PAGE_PARAMETER_ID).toInt(1)
}
