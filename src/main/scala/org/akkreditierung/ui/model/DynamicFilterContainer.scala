package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query=>EbeanQuery}
import org.apache.wicket.markup.html.form.{FormComponent, TextField}
import collection.mutable.Buffer
import scala.slick.lifted.Query
import org.akkreditierung.model.DB
import scala.slick.driver.H2Driver.simple._
import scala.slick.session.Database
import Database.threadLocalSession

case class Filter(id: String, field: FormComponent[String], filter: (String, ExpressionList[_]) => _)
case class FilterSlick[E,T](id: String, field: FormComponent[String], filter: (String, Query[E,T]) => Query[E,T])

object DynamicFilterContainer {
  def likeFilter(field: String) = (value: String, where: ExpressionList[_]) => where.like(field, value)
  def likeAttributeFilter(field: String) = (value: String, where: ExpressionList[_]) => where.and(Expr.eq("map.k", field), Expr.like("map.v", value))
}
class DynamicFilterContainer[E, T] extends DBFilter[T] with SlickFilter[E,T] {

  val filters: Buffer[Filter]= Buffer[Filter]()
  val slickFilters: Buffer[FilterSlick[E,T]]= Buffer[FilterSlick[E,T]]()

  def add(filter: Filter) {
    filters+=filter
  }

  def add(filter: FilterSlick[E,T]) {
    slickFilters+=filter
  }

  override def apply(query: EbeanQuery[T]) {
    val where: ExpressionList[T] = query.where
    DB.db withSession {
      filters.foreach{filter=>
        filterIfNotEmpty(filter.field, query, filter.filter)
      }
    }
  }

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

  def filterIfNotEmpty(field: FormComponent[String], query: EbeanQuery[T], filter: (String, ExpressionList[_]) => _) {
    if(isNotEmpty(field)) {
      filter(s"%${field.getValue}%", query.where())
    }
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