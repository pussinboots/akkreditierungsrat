package org.akkreditierung.ui.model

import scala.slick.lifted.{Column => SlickColumn, Query => SlickQuery, ColumnOrdered}
import scala.slick.session.{ Database, Session }
import Database.threadLocalSession
import org.apache.wicket.extensions.markup.html.repeater.util.{SortParam, SortableDataProvider}
import org.apache.wicket.model.IModel
import org.apache.wicket.model.LoadableDetachableModel
import org.akkreditierung.model.DB
import DB.dal.profile.simple._

@SerialVersionUID(-6117562733583734933L)
class GenericSlickProvider[E<: Table[T], T](query: SlickQuery[E, T]) extends SortableDataProvider[T, String] {

  def model(value: T): IModel[T] = new LoadableDetachableModel[T] { protected def load: T = value }

  def filter(query: SlickQuery[E, T]): SlickQuery[E, T] = query

  def iterator(first: Long, count: Long): java.util.Iterator[T] = {
    DB.db withSession {
      import collection.JavaConversions._
      import DB.dal._
      import DB.dal.profile.simple._
      val q = filter(query).drop(first.toInt).take(count.toInt).sortBy(e=>sortKey(e, getSort))
      println("SlickProvider " + query.selectStatement)
      println("SlickProvider " + q.selectStatement)
      val list: java.util.List[T] = q.list
      list.iterator
    }
  }

  def size: Long = {
    DB.db withSession {
      val q = SlickQuery(filter(query).length)
      import DB.dal._
      import DB.dal.profile.simple._
      q.first
    }
  }

  def sortKey[T](e:E, param: SortParam[String]): ColumnOrdered[_] = {
    param.isAscending match {
      case true =>  e.column[String](param.getProperty).asc
      case false => e.column[String](param.getProperty).desc
    }
  }
}