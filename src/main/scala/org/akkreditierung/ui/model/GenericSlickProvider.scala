package org.akkreditierung.ui.model

import scala.slick.lifted.{ColumnOrdered}
import scala.slick.session.{Database, Session}
import Database.threadLocalSession
import org.apache.wicket.extensions.markup.html.repeater.util.{SortParam, SortableDataProvider}
import org.apache.wicket.model.{IModel, LoadableDetachableModel}
import org.akkreditierung.model.DB
import DB.dal.profile.simple._
import DB.dal.profile.simple.{Query => SlickQuery}
@SerialVersionUID(-6117562733583734933L)
class GenericSlickProvider[E <: Table[T], T](query: SlickQuery[E, T]) extends SortableDataProvider[T, String] {

  def model(value: T): IModel[T] = new LoadableDetachableModel[T] { protected def load: T = value }

  def filter(query: SlickQuery[E, T]): SlickQuery[E, T] = query

  import collection.JavaConversions._
  def iterator(first: Long, count: Long): java.util.Iterator[T] = DB.db withSession {
    filter(query).sortBy(sortKey(_, getSort)).drop(first.toInt).take(count.toInt).list.iterator
  }

  def size: Long = DB.db withSession SlickQuery(filter(query).length).first

  def sortKey[T](e: E, param: SortParam[String]): ColumnOrdered[_] = param.isAscending match {
    case true => e.column[String](param.getProperty).asc
    case false => e.column[String](param.getProperty).desc
  }
}