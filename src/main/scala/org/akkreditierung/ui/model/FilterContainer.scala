package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query=>EbeanQuery}
import org.apache.wicket.markup.html.form.{FormComponent, TextField}
import scala.slick.lifted.Query

trait DBFilter[T] {
  def apply(query: EbeanQuery[T])
}

trait SlickFilter[E,T] {
  def apply(query: Query[E,T]): Query[E, T]
}

class FilterContainer[T](hochSchule: TextField[String], fach: TextField[String], abschluss: TextField[String], agentur: TextField[String], studienForm: TextField[String], jobId: TextField[String]) extends DBFilter[T] {

  override def apply(query: EbeanQuery[T]) {
    val where: ExpressionList[T] = query.where
    filterIfNotEmpty(hochSchule, {(value:String,field:String)=> where.like(field, value)})
    filterIfNotEmpty(fach, {(value:String,field:String) => where.like("fach", value)})
    filterIfNotEmpty(abschluss, {(value:String,field:String) => where.like("abschluss", value)})
    filterIfNotEmpty(jobId, {(value:String,field:String) => where.eq("jobId", value.substring(1, value.length - 1).toInt)})
    filterIfNotEmpty(agentur, {(value:String,field:String) => where.and(Expr.eq("map.k", "von"), Expr.like("map.v", value))})
    filterIfNotEmpty(studienForm, {(value:String,field:String) =>  where.and(Expr.eq("map.k", "Besondere Studienform"), Expr.like("map.v", value))})
  }

  def isNotEmpty(field: FormComponent[String]) = {
    field.getValue != null && field.getValue.length > 0
  }

  def filterIfNotEmpty(field: FormComponent[String], filter: (String, String) => _) {
    if(isNotEmpty(field)) {
      filter(s"%${field.getValue}%",field.getId)
    }
  }
}