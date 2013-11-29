package org.akkreditierung.ui.model

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder
import org.akkreditierung.model.slick.{Studiengang, DAL}
import scala.slick.lifted.Query
import org.akkreditierung.model.DB

@SerialVersionUID(-6117562733583734933L)
class StudienGangModelProvider(filterContainer: SlickFilter[DB.dal.Studiengangs.type , Studiengang]) extends GenericSlickProvider[DB.dal.Studiengangs.type, Studiengang](Query(DB.dal.Studiengangs)) {
  setSort("fach", SortOrder.ASCENDING)

  override def filter(query: Query[DB.dal.Studiengangs.type, Studiengang]) = {
    val _query = if (filterContainer != null) filterContainer.apply(query) else query
    super.filter(_query)
  }
}