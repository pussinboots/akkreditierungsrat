package org.akkreditierung.ui.model

import com.avaje.ebean.OrderBy
import com.avaje.ebean.Query
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import java.util.Iterator
import java.util.List

@SerialVersionUID(-6117562733583734933L)
class GenericProvider[T](bean: DefaultBean[T]) extends SortableDataProvider[T, String] {

  def model(value: T): IModel[T] = new LoadableDetachableModel[T] { protected def load: T = value }

  private def orderBy(param: SortParam[String]): OrderBy[T] = {
    val orderBy: OrderBy[T] = new OrderBy[T]
    if (param.isAscending) orderBy.asc(param.getProperty) else orderBy.desc(param.getProperty)
    orderBy
  }

  def filter(query: Query[T]): Query[T] = query

  def iterator(first: Long, count: Long): Iterator[T] = {
    val list: List[T] = filter(bean.getQuery).setOrderBy(orderBy(getSort)).setMaxRows(count.toInt).setFirstRow(first.toInt).findList
    list.iterator
  }

  def size: Long = filter(bean.getQuery).findRowCount
}