package org.akkreditierung.ui.model

import com.avaje.ebean.OrderBy
import com.avaje.ebean.Query
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import java.util.Iterator
import java.util.List

class GenericProvider[T] extends SortableDataProvider[T, String] {
  private final val serialVersionUID: Long = -6117562733583734933L

  def this(defaultBean: DefaultBean[T]) {
    this()
    this.bean = defaultBean
  }

  def model(value: T): IModel[T] = {
    return new LoadableDetachableModel[T] {
      protected def load: T = {
        return value
      }
    }
  }

  private def orderBy(param: SortParam[String]): OrderBy[T] = {
    val orderBy: OrderBy[T] = new OrderBy[T]
    if (param.isAscending) {
      orderBy.asc(param.getProperty)
    }
    else {
      orderBy.desc(param.getProperty)
    }
    return orderBy
  }

  def filter(query: Query[T]): Query[T] = {
    return query
  }

  def iterator(first: Long, count: Long): Iterator[T] = {
    val list: List[T] = filter(bean.getQuery).setOrderBy(orderBy(getSort)).setMaxRows(count.toInt).setFirstRow(first.toInt).findList
    return list.iterator
  }

  def size: Long = {
    return filter(bean.getQuery).findRowCount
  }

  private final var bean: DefaultBean[T] = null
}