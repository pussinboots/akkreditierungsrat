package org.akkreditierung.ui.model

import com.avaje.ebean.ExpressionList
import com.avaje.ebean.Query
import org.apache.wicket.markup.html.form.TextField

class FilterContainer {
  def this(hochSchule: TextField[String], fach: TextField[String], abschluss: TextField[String]) {
    this()
    this.hochSchule = hochSchule
    this.fach = fach
    this.abschluss = abschluss
  }

  def getHochSchule: String = {
    return hochSchule.getValue
  }

  def getFach: String = {
    return fach.getValue
  }

  def getAbschluss: String = {
    return abschluss.getValue
  }

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
  }

  private var hochSchule: TextField[String] = null
  private var fach: TextField[String] = null
  private var abschluss: TextField[String] = null
}