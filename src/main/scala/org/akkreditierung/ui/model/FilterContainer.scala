package org.akkreditierung.ui.model

import com.avaje.ebean.{Expr, ExpressionList, Query}
import org.apache.wicket.markup.html.form.TextField

class FilterContainer(hochSchule: TextField[String], fach: TextField[String], abschluss: TextField[String], agentur: TextField[String], studienForm: TextField[String]) {

  def getHochSchule: String = return s"%${hochSchule.getValue}"
  def getFach: String = return s"%${fach.getValue}"
  def getAbschluss: String = return s"%${abschluss.getValue}"
  def getAgentur: String = return s"%${agentur.getValue}"
  def getStudienForm: String = return s"%${studienForm.getValue}"

  def apply[T](query: Query[T]) {
    val where: ExpressionList[T] = query.where
    if (getHochSchule != null && getHochSchule.length > 0) {
      where.like("hochschule", getHochSchule)
    }
    if (getFach != null && getFach.length > 0) {
      where.like("fach", getFach)
    }
    if (getAbschluss != null && getAbschluss.length > 0) {
      where.like("abschluss", getAbschluss)
    }
    if (getAgentur != null && getAgentur.length > 0) {
      where.and(Expr.eq("map.k", "von"), Expr.like("map.v", getAgentur))
    }
    if (getStudienForm != null && getStudienForm.length > 0) {
      where.and(Expr.eq("map.k", "Besondere Studienform"), Expr.like("map.v", getStudienForm))
    }
  }
}