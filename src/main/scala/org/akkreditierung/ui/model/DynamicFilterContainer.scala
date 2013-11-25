package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query}
import org.apache.wicket.markup.html.form.{FormComponent, TextField}
import collection.mutable.Buffer


case class Filter(id: String, field: FormComponent[String], filter: (String, ExpressionList[_]) => _)
object DynamicFilterContainer {
  def likeFilter(field: String) = (value: String, where: ExpressionList[_]) => where.like(field, value)
  def likeAttributeFilter(field: String) = (value: String, where: ExpressionList[_]) => where.and(Expr.eq("map.k", field), Expr.like("map.v", value))
}
class DynamicFilterContainer extends DBFilter {

  val filters: Buffer[Filter]= Buffer[Filter]()

  def add(filter: Filter) {
    filters+=filter
  }

  override def apply[T](query: Query[T]) {
    val where: ExpressionList[T] = query.where
    filters.foreach{filter=>
      filterIfNotEmpty(filter.field, query, filter.filter)
    }
  }

  def isNotEmpty(field: FormComponent[String]) = {
    field.getValue != null && field.getValue.length > 0
  }

  def filterIfNotEmpty[T](field: FormComponent[String], query: Query[T], filter: (String, ExpressionList[_]) => _) {
    if(isNotEmpty(field)) {
      filter(s"%${field.getValue}%", query.where())
    }
  }
}