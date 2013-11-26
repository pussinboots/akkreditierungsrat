package org.akkreditierung.ui.page

import org.akkreditierung.ui.model._
import org.apache.wicket.MarkupContainer
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.markup.html.AjaxLink
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn
import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.link.{BookmarkablePageLink, ExternalLink, Link}
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.repeater.data.ListDataProvider
import org.apache.wicket.model.{IModel, PropertyModel}
import org.apache.wicket.request.mapper.parameter.PageParameters
import java.util._
import org.wicket.scala.Columns._
import org.wicket.scala.Fields._
import org.wicket.scala.RepeatingViews._
import org.apache.wicket.markup.html.form.Form
import com.avaje.ebean.{Expr, ExpressionList}
import org.akkreditierung.ui.model.Filter
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick.{Studiengang, Job}

object AdvertiserConfigPage {
  private final val serialVersionUID: Long = 1L
}

class AdvertiserConfigPage(parameters: PageParameters) extends WebPage(parameters) {
  val detailPanel: MarkupContainer = new DetailPanel("selected", new PropertyModel[Studiengang](this, "selected"))
  add(detailPanel)
  val filterContainer = createFilterContainer()
  DB.db withSession{
    val job = DB.dal.Jobs.findLatest().getOrElse(Job(1))
    add(new BookmarkablePageLink[Void]("editLink", classOf[StudiengangEditPage]))
    add(new Label("job", s" ${job.newEntries} neue Studiengänge importiert am ${job.createDate} status ${job.status}"))
    val table = new DefaultDataTable[Studiengang, String]("datatable", getColumns(detailPanel), new StudienGangModelProvider(filterContainer), 25)
    add(table)
  }

  private def createFilterContainer() = {
    val form = new Form("filterForm")
    val filter = new DynamicFilterContainer[DB.dal.Studiengangs.type, Studiengang]()
    import DB.dal.profile.simple._
    def s = {
      (value: String, query: Query[DB.dal.Studiengangs.type, Studiengang]) =>
        query.filter(_.hochschule like value)
    }
    filter.add(new FilterSlick[DB.dal.Studiengangs.type, Studiengang]("hochschule", createAjaxTextFilter("hochschule", form), s))
    filter.add(new FilterSlick[DB.dal.Studiengangs.type, Studiengang]("fach", createAjaxTextFilter("fach", form), (value: String, query: Query[DB.dal.Studiengangs.type, Studiengang]) => query.filter(_.fach like value)))
    filter.add(new FilterSlick[DB.dal.Studiengangs.type, Studiengang]("abschluss", createAjaxTextFilter("abschluss", form), (value: String, query: Query[DB.dal.Studiengangs.type, Studiengang]) => query.filter(_.abschluss like value)))
    filter.add(new FilterSlick[DB.dal.Studiengangs.type, Studiengang]("agentur", createAjaxTextFilter("agentur", form), (value: String, query: Query[DB.dal.Studiengangs.type, Studiengang]) => query.filter(_.hochschule like value)))
    filter.add(new FilterSlick[DB.dal.Studiengangs.type, Studiengang]("studienform", createAjaxTextFilter("studienform", form), (value: String, query: Query[DB.dal.Studiengangs.type, Studiengang]) => query.filter(_.hochschule like value)))
    filter.add(new FilterSlick[DB.dal.Studiengangs.type, Studiengang]("jobId", createAjaxHiddenTextFilter("jobId", form), (value: String, query: Query[DB.dal.Studiengangs.type, Studiengang]) => query.filter(_.jobId === value.substring(1, value.length - 1).toInt)))
    add(form)
    filter
  }

  private def getColumns(detailPanel: MarkupContainer) = {
    val columns = new ArrayList[IColumn[Studiengang, String]]
    columns.add(column("Id", "id"))
    columns.add(column("Fach", "fach"))
    columns.add(column("Abschluss", "abschluss"))
    columns.add(column("Hochschule", "hochschule"))
    columns.add(column("Bezugstyp", "bezugstyp"))
    columns.add(column("Gutachten", "gutachtenLink", (item, componentId, rowModel) => new LinkPanel(componentId, rowModel.getObject.gutachtentLink.getOrElse(null), "hier")))
    columns.add(column("Änderungs Datum", "modifiedDate"))
    columns.add(column("Aktion", "id", (item, componentId, rowModel) => new ActionPanel(componentId, rowModel, detailPanel)))
    columns
  }

  def getSelected: Studiengang = {
    selected
  }

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
        val pageParameters: PageParameters = new PageParameters
        pageParameters.add(StudiengangDetailPage.PAGE_PARAMETER_ID, model.getObject.id.get)
        setResponsePage(classOf[StudiengangDetailPage], pageParameters)
      }
    })
  }

  private class DetailPanel(id: String, model: IModel[Studiengang]) extends Panel(id, model) {
    setOutputMarkupId(true)
    val mapModel: IModel[Map[String, StudiengaengeAttribute]] = new PropertyModel[Map[String, StudiengaengeAttribute]](model, "attributes")
    val provider: ListDataProvider[StudiengaengeAttribute] = new ListDataProvider[StudiengaengeAttribute] {
      protected override def getData: List[StudiengaengeAttribute] = {
        val map: Map[String, StudiengaengeAttribute] = if ((mapModel.getObject == null)) Collections.emptyMap[String, StudiengaengeAttribute] else mapModel.getObject
        return new ArrayList[StudiengaengeAttribute](map.values)
      }
    }

    val d = dataView("displayPanel", provider) {(entry: StudiengaengeAttribute, item: Item[StudiengaengeAttribute]) =>
      item.add(new Label("key_column", entry.getK))
      item.add(labelWithSpecialEscaping("value_column", entry.getV))
    }

    add(d)
  }
}