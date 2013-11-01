package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query}
import org.apache.wicket.markup.html.form.{FormComponent, TextField}
import java.sql.Connection
import scalikejdbc.ConnectionPool

class FilterContainer(hochSchule: TextField[String], fach: TextField[String], abschluss: TextField[String], agentur: TextField[String], studienForm: TextField[String], jobId: TextField[String]) {

  def apply[T](query: Query[T]) {
    val where: ExpressionList[T] = query.where
    filterIfNotEmpty(hochSchule, {value: String => where.like("hochschule", value)})
    filterIfNotEmpty(fach, {value: String => where.like("fach", value)})
    filterIfNotEmpty(abschluss, {value: String => where.like("abschluss", value)})
    filterIfNotEmpty(jobId, {value: String => where.eq("jobId", value.substring(1, value.length - 1).toInt)})
    filterIfNotEmpty(agentur, {value: String => where.and(Expr.eq("map.k", "von"), Expr.like("map.v", value))})
    filterIfNotEmpty(studienForm, {value: String =>  where.and(Expr.eq("map.k", "Besondere Studienform"), Expr.like("map.v", value))})
  }

  def isNotEmpty(field: FormComponent[String]) = {
    field.getValue != null && field.getValue.length > 0
  }

  def filterIfNotEmpty(field: FormComponent[String], filter: String => _) {
    if(isNotEmpty(field)) {
      filter(s"%${field.getValue}%")
    }
  }
}