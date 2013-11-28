package org.akkreditierung.ui.page

import org.akkreditierung.ui.model._
import org.apache.wicket.MarkupContainer
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn
import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.link.{ExternalLink, Link}
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.model.{IModel, PropertyModel}
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.util._
import org.wicket.scala.Columns._
import org.wicket.scala.Fields._
import org.wicket.scala.RepeatingViews._
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick._
import org.wicket.scala.WicketDSL._

object AdvertiserConfigPage {
  private final val serialVersionUID: Long = 1L
}

class AdvertiserConfigPage(parameters: PageParameters) extends WebPage(parameters) {
  val detailPanel = "selected".panel(this, "selected",(id:String, model:IModel[Studiengang])=>new DetailPanel(id, model))(this)
  val job = DB.db withSession DB.dal.Jobs.findLatest().getOrElse(Job(1))
  add("editLink".pageLink(classOf[StudiengangEditPage]))
  add("job".label(s" ${job.newEntries} neue Studiengänge importiert am ${job.createDate} status ${job.status}"))
  add("datatable".table(getColumns, new StudienGangModelProvider(createFilterContainer()), detailPanel))

  private def createFilterContainer() = {
    val form = "filterForm".form(this)
    import DynamicFilterContainer._
    val filter = new DynamicFilterContainer[DB.dal.Studiengangs.type, Studiengang]()
    filter.add("hochschule", "hochschule".textField(form), likeFilter("hochschule"))
    filter.add("fach", "fach".textField(form), likeFilter("fach"))
    filter.add("abschluss", "abschluss".textField(form), likeFilter("abschluss"))
    filter.add("agentur", "agentur".textField(form), likeAttributeFilter("von"))
    filter.add("studienform", "studienform".textField(form), likeAttributeFilter("Besondere Studienform"))
    filter.add("jobId", "jobId".hiddenTextField(form), likeFilter("abschluss", (value:String) => value.substring(1, value.length - 1)))
    filter
  }

  private def getColumns(detailPanel: MarkupContainer) = {
    val columns = new ArrayList[IColumn[Studiengang, String]]
    "Id".addColumn(columns)
    "Fach".addColumn(columns)
    "Abschluss".addColumn(columns)
    "Hochschule".addColumn(columns)
    "Bezugstyp".addColumn(columns)
    columns.add(column("Gutachten", "gutachtenLink", (item, componentId, rowModel) => new LinkPanel(componentId, rowModel.getObject.gutachtenLink.getOrElse(null), "hier")))
    "Änderungs Datum".addColumn("modifiedDate", columns)
    columns.add(column("Aktion", "id", (item, componentId, rowModel) => new ActionPanel(componentId, rowModel, detailPanel)))
    columns
  }

  def getSelected: Studiengang = selected

  def setSelected(selected: Studiengang) {
    addStateChange
    this.selected = selected
  }

  private var selected: Studiengang = null

  private class LinkPanel(id: String, url: String, label: String) extends Panel(id) {
    var link = if (url != null) {new ExternalLink("link", url, label)} else {new Label("link")}
    add(link)
  }

  private class ActionPanel(id: String, model: IModel[Studiengang], detailPanel: MarkupContainer) extends Panel(id, model) {
    add(new AjaxLink(("select")) {
      def onClick(target: AjaxRequestTarget) {
        setSelected(getParent.getDefaultModelObject.asInstanceOf[Studiengang])
        target.add(detailPanel)
      }
    })
    add(new Link(("new")) {
      def onClick {
        val pageParameters: PageParameters = new PageParameters().add(StudiengangDetailPage.PAGE_PARAMETER_ID, model.getObject.id.get)
        setResponsePage(classOf[StudiengangDetailPage], pageParameters)
      }
    })
  }

  class DetailPanel(id: String, model: IModel[Studiengang]) extends Panel(id, model) {
    setOutputMarkupId(true)
    val mapModel: IModel[Map[String, StudiengangAttribute]] = new PropertyModel[Map[String, StudiengangAttribute]](model, "attributes")
    val provider: ListDataProvider[StudiengangAttribute] = new ListDataProvider[StudiengangAttribute] {
      protected override def getData: List[StudiengangAttribute] = {
        val map: Map[String, StudiengangAttribute] = if ((mapModel.getObject == null)) Collections.emptyMap[String, StudiengangAttribute] else mapModel.getObject
        return new ArrayList[StudiengangAttribute](map.values)
      }
    }
    val d = dataView("displayPanel", provider) {(entry: StudiengangAttribute, item: Item[StudiengangAttribute]) =>
      item.add(new Label("key_column", entry.key))
      item.add(labelWithSpecialEscaping("value_column", entry.value))
    }
    add(d)
  }
}