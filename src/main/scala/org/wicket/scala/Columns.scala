package org.wicket.scala

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn
import org.apache.wicket.model.{IModel, Model}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator
import org.apache.wicket.Component

object Columns {
  def column[T](label: String, field: String) = new PropertyColumn[T, String](new Model[String](label), field, field)

  def column[T](label: String, field: String, block: (Item[ICellPopulator[T]], String, IModel[T]) => Component) = {
    new PropertyColumn[T, String](new Model[String](label), field, field) {
      override def populateItem(item: Item[ICellPopulator[T]], componentId: String, rowModel: IModel[T]) {
        item.add(block(item, componentId, rowModel))
      }
    }
  }
}
