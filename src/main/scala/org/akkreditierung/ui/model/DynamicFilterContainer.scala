package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query=>EbeanQuery}
import org.apache.wicket.markup.html.form.{FormComponent, TextField}
import collection.mutable.Buffer
import scala.slick.lifted.Query
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick.Studiengang
import DB.dal.profile.simple._
import scala.slick.session.Database
import Database.threadLocalSession

case class FilterSlick[E,T](id: String, field: FormComponent[String], filter:(String, Query[E,T])  => Query[E,T])

trait SlickFilter[E,T] {
  def apply(query: Query[E,T]): Query[E, T]

  def add(id: String, field: FormComponent[String], filter:(String, Query[E,T])  => Query[E,T])
}

object DynamicFilterContainer {
  def likeFilter[E<: Table[T],T](field: String) = {(value: String, query: Query[E, T]) =>
      query.filter(_.column[String](field) like value)
  }
  def likeAttributeFilter(field: String) = {(value: String, query: Query[DB.dal.Studiengangs.type, Studiengang]) =>
    query.flatMap{c=>
      DB.dal.StudiengangAttributes.filter(_.id ===c.id).filter(_.key === field).filter(_.value like value).map(s =>(c))
    }
  }
  def likeFilter[E<: Table[T],T, R](field: String, valueTranformer:(String)=>R) = {(value: String, query: Query[E,T]) =>
    query.filter(_.column[String](field) like valueTranformer(value))
  }
}
class DynamicFilterContainer[E, T] extends SlickFilter[E,T] {

  val slickFilters: Buffer[FilterSlick[E,T]]= Buffer[FilterSlick[E,T]]()

  override def add(id: String, field: FormComponent[String], filter:(String, Query[E,T])  => Query[E,T]) = add(new FilterSlick[E, T](id, field, filter))

  def add(filter: FilterSlick[E,T]) = slickFilters+=filter

  override def apply(query: Query[E,T]): Query[E,T] = {
    var _query = query
    slickFilters.foreach{filter:FilterSlick[E,T]=>
      _query = filterIfNotEmpty(filter.field, _query, filter.filter)
    }
    _query
  }

  def isNotEmpty(field: FormComponent[String]) = {
    field.getValue != null && field.getValue.length > 0
  }

  def filterIfNotEmpty(field: FormComponent[String], query: Query[E, T], filter: (String, Query[E,T]) => Query[E, T]): Query[E, T] = {
    if(isNotEmpty(field)) {
      DB.db withSession {
        return filter(s"%${field.getValue}%", query)
      }
    }
    return query
  }
}