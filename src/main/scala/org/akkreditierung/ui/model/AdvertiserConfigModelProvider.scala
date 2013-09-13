package org.akkreditierung.ui.model

import com.avaje.ebean.{FetchConfig, Query}
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder

object AdvertiserConfigModelProvider {

}

class AdvertiserConfigModelProvider extends GenericProvider[AdvertiserConfig](new AdvertiserConfigBean) {
  private final val serialVersionUID: Long = -6117562733583734933L

  def this(filterContainer: FilterContainer) {
    this()
    this.filterContainer = filterContainer
    setSort("id", SortOrder.ASCENDING)
  }

  override def filter(query: Query[AdvertiserConfig]): Query[AdvertiserConfig] = {
    filterContainer.apply(query)
    query.fetch("map", new FetchConfig().`lazy`())
    return super.filter(query)
  }

  private var filterContainer: FilterContainer = null
}