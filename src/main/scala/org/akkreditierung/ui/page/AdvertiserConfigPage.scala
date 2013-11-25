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

object AdvertiserConfigPage {
  private final val serialVersionUID: Long = 1L
}

class AdvertiserConfigPage(parameters: PageParameters) extends WebPage(parameters) {
  val detailPanel: MarkupContainer = new DetailPanel("selected", new PropertyModel[AdvertiserConfig](this, "selected"))
  add(detailPanel)
  val filterContainer: DBFilter = createFilterContainer()
  val job = new JobBean().findLatest()
  add(new BookmarkablePageLink[Void]("editLink", classOf[StudiengangEditPage]))
  add(new Label("job", s" ${job.getNewEntries()} neue Studiengänge importiert am ${job.createDate} status ${job.status}"))
  val table = new DefaultDataTable[AdvertiserConfig, String]("datatable", getColumns(detailPanel), new AdvertiserConfigModelProvider(filterContainer), 25)
  add(table)

  private def createFilterContainer() : DBFilter = {
    val form = new Form("filterForm")
    //val filter = new FilterContainer(createAjaxTextFilter("hochschule", form), createAjaxTextFilter("fach", form), createAjaxTextFilter("abschluss", form), createAjaxTextFilter("agentur", form), createAjaxTextFilter("studienform", form), createAjaxHiddenTextFilter("jobId", form))
    import DynamicFilterContainer._
    val filter = new DynamicFilterContainer()
    filter.add(new Filter("hochschule", createAjaxTextFilter("hochschule", form), likeFilter("hochschule")))
    filter.add(new Filter("fach", createAjaxTextFilter("fach", form), likeFilter("fach")))
    filter.add(new Filter("abschluss", createAjaxTextFilter("abschluss", form), likeFilter("abschluss")))
    filter.add(new Filter("agentur", createAjaxTextFilter("agentur", form), likeAttributeFilter("von")))
    filter.add(new Filter("studienform", createAjaxTextFilter("studienform", form), likeAttributeFilter("Besondere Studienform")))
    filter.add(new Filter("jobId", createAjaxHiddenTextFilter("jobId", form), {(value: String, where: ExpressionList[_]) => where.eq("jobId", value.substring(1, value.length - 1).toInt)}))
    add(form)
    filter
  }

  private def getColumns(detailPanel: MarkupContainer) = {
    val columns = new ArrayList[IColumn[AdvertiserConfig, String]]
    columns.add(column("Id", "id"))
    columns.add(column("Fach", "fach"))
    columns.add(column("Abschluss", "abschluss"))
    columns.add(column("Hochschule", "hochschule"))
    columns.add(column("Bezugstyp", "bezugstyp"))
    columns.add(column("Gutachten", "gutachtenLink", (item, componentId, rowModel) => new LinkPanel(componentId, rowModel.getObject.getGutachtenLink, "hier")))
    columns.add(column("Änderungs Datum", "modifiedDate"))
    columns.add(column("Aktion", "id", (item, componentId, rowModel) => new ActionPanel(componentId, rowModel, detailPanel)))
    columns
  }

  def getSelected: AdvertiserConfig = {
    selected
  }

  def setSelected(selected: AdvertiserConfig) {
    addStateChange
    this.selected = selected
  }

  private var selected: AdvertiserConfig = null

  private class LinkPanel(id: String, url: String, label: String) extends Panel(id) {
    var link = if (url != null) {new ExternalLink("link", url, label)} else {new Label("link")}
    add(link)
  }

  private class ActionPanel(id: String, model: IModel[AdvertiserConfig], detailPanel: MarkupContainer) extends Panel(id, model) {
    add(new AjaxLink(("select")) {
      def onClick(target: AjaxRequestTarget) {
        setSelected(getParent.getDefaultModelObject.asInstanceOf[AdvertiserConfig])
        target.add(detailPanel)
      }
    })
    add(new Link(("new")) {
      def onClick {
        val pageParameters: PageParameters = new PageParameters
        pageParameters.add(StudiengangDetailPage.PAGE_PARAMETER_ID, model.getObject.getId)
        setResponsePage(classOf[StudiengangDetailPage], pageParameters)
      }
    })
  }

  private class DetailPanel(id: String, model: IModel[AdvertiserConfig]) extends Panel(id, model) {
    setOutputMarkupId(true)
    val mapModel: IModel[Map[String, StudiengaengeAttribute]] = new PropertyModel[Map[String, StudiengaengeAttribute]](model, "map")
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