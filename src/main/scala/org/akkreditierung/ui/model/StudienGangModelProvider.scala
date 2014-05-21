package org.akkreditierung.ui.model

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder
import org.akkreditierung.model.slick._
import scala.slick.lifted.TableQuery
import org.akkreditierung.model.DB
import org.akkreditierung.model.slick.Studiengang
import org.akkreditierung.model.slick._
import DB.dal._
import DB.dal.profile.simple._

@SerialVersionUID(-6117562733583734933L)
class StudienGangModelProvider(filterContainer: SlickFilter[DB.dal.Studiengangs, Studiengang]) extends GenericSlickProvider[DB.dal.Studiengangs, Studiengang](studiengangs) {
  setSort("fach", SortOrder.ASCENDING)

  override def filter(query: Query[DB.dal.Studiengangs, Studiengang]) = {
    val _query = if (filterContainer != null) filterContainer.apply(query) else query
    super.filter(_query)
  }
}
