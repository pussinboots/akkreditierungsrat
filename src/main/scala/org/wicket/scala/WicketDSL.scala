package org.wicket.scala

import org.apache.wicket.{Page, MarkupContainer}
import org.apache.wicket.markup.html.form.Form
import org.wicket.scala.Fields._
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.link.BookmarkablePageLink
import org.apache.wicket.extensions.markup.html.repeater.data.table.{DefaultDataTable, ISortableDataProvider, IColumn}
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.model.{PropertyModel, IModel}
import java.util.ArrayList
import org.wicket.scala.Columns._

class WicketDSL(id: String) {
  def add[T<:MarkupContainer](markupContainerToAdd: T, markupContainer: MarkupContainer) = {
    markupContainer.add(markupContainerToAdd)
    markupContainerToAdd
  }
  def form(markupContainer: MarkupContainer): Form[_] = add(new Form(id), markupContainer)
  def textField[T](form:Form[T]) = createAjaxTextFilter(id, form)
  def hiddenTextField[T](form:Form[T]) = createAjaxHiddenTextFilter(id, form)
  def label(message: String) = new Label(id, message)
  def pageLink[T<:Page](clazz: Class[T]) = new BookmarkablePageLink[Void](id, clazz)
  def table[T,M,C <:IColumn[T, String]](f:(M)=>java.util.List[C], provider: ISortableDataProvider[T, String], markup: M) = new DefaultDataTable[T, String](id, f(markup), provider, 25)
  def panel[T, P<:Panel](modelObject: Object, field: String, createPanel:(String, IModel[T])=>P)(markupContainer: MarkupContainer): P = add(createPanel(id, new PropertyModel[T](modelObject, field)), markupContainer)
  def addColumn[T](columns: ArrayList[IColumn[T, String]]) { addColumn(id.toLowerCase, columns) }
  def addColumn[T](field:String, columns: ArrayList[IColumn[T, String]]) { columns.add(column(id, field)) }
}
object WicketDSL {
  implicit def stringWicket(tap: String) = new WicketDSL(tap)
}
