package org.akkreditierung.ui.model

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import java.util.Iterator
import org.akkreditierung.model.DBBean

//TODO test class
@SerialVersionUID(-6117562733583734933L)
class GenericScalaProvider[T](bean: DBBean[T]) extends SortableDataProvider[T, String] {

  def model(value: T): IModel[T] = new LoadableDetachableModel[T] { protected def load: T = value }

  def iterator(first: Long, count: Long): Iterator[T] = {
    import scala.collection.JavaConversions._
    bean.Find(count, first).iterator
  }

  def size: Long = bean.Count()
}