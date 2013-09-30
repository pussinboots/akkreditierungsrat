package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query}
import org.apache.wicket.markup.html.form.{FormComponent, TextField}

class FilterContainer(hochSchule: TextField[String], fach: TextField[String], abschluss: TextField[String], agentur: TextField[String], studienForm: TextField[String]) {

  def getHochSchule: String = return s"%${hochSchule.getValue}"
  def getFach: String = return s"%${fach.getValue}"
  def getAbschluss: String = return s"%${abschluss.getValue}"
  def getAgentur: String = return s"%${agentur.getValue}"
  def getStudienForm: String = return s"%${studienForm.getValue}"

  def apply[T](query: Query[T]) {
    val where: ExpressionList[T] = query.where
    if (isNotEmpty(hochSchule)) {
      where.like("hochschule", getHochSchule)
    }
    if (isNotEmpty(fach)) {
      where.like("fach", getFach)
    }
    if (isNotEmpty(abschluss)) {
      where.like("abschluss", getAbschluss)
    }
    if (isNotEmpty(agentur)) {
      where.and(Expr.eq("map.k", "von"), Expr.like("map.v", getAgentur))
    }
    if (isNotEmpty(studienForm)) {
      where.and(Expr.eq("map.k", "Besondere Studienform"), Expr.like("map.v", getStudienForm))
    }
  }

  def isNotEmpty(field: FormComponent[String]) = {
    field.getValue != null && field.getValue.length > 0
  }
}