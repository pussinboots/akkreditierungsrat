package org.wicket.scala

import org.apache.wicket.markup.repeater.data.{IDataProvider, DataView}
import org.apache.wicket.markup.repeater.Item
import org.apache.wicket.markup.html.list.{ListItem, ListView}

object RepeatingViews {

  def dataView[T](componentId: String, provider: IDataProvider[T])(block: (T, Item[T]) => Unit) = {
    val dataView: DataView[T] = new DataView[T](componentId, provider) {
      protected def populateItem(item: Item[T]) {
        val entry: T = item.getModelObject
        block(entry, item)
      }
    }
    dataView
  }

  def listView[T](componentId: String, list: java.util.List[T])(block: (T, ListItem[T]) => Unit) = {
    val listView: ListView[T] = new ListView[T](componentId, list) {
      protected def populateItem(item: ListItem[T]) {
        val entry: T = item.getModelObject
        block(entry, item)
      }
    }
    listView
  }
}
