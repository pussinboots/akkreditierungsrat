package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query}
import org.apache.wicket.markup.html.form.{FormComponent, TextField}
import java.sql.Connection
import scalikejdbc.ConnectionPool

class FilterContainer(hochSchule: TextField[String], fach: TextField[String], abschluss: TextField[String], agentur: TextField[String], studienForm: TextField[String]) {

  def apply[T](query: Query[T]) {
    val where: ExpressionList[T] = query.where
    filterIfNotEmpty(hochSchule, value => where.like("hochschule", value))
    filterIfNotEmpty(fach, value => where.like("fach", value))
    filterIfNotEmpty(abschluss, value => where.like("abschluss", value))
    filterIfNotEmpty(agentur, value => where.and(Expr.eq("map.k", "von"), Expr.like("map.v", value)))
    filterIfNotEmpty(studienForm, value =>  where.and(Expr.eq("map.k", "Besondere Studienform"), Expr.like("map.v", value)))
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