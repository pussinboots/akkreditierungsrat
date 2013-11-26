package org.akkreditierung.ui.model

import com.avaje.ebean.Query
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder

@SerialVersionUID(-6117562733583734933L)
class AdvertiserConfigModelProvider(filterContainer: DBFilter[AdvertiserConfig]) extends GenericProvider[AdvertiserConfig](new AdvertiserConfigBean) {
  setSort("fach", SortOrder.ASCENDING)

  override def filter(query: Query[AdvertiserConfig]): Query[AdvertiserConfig] = {
    if (filterContainer != null) filterContainer.apply(query)
    super.filter(query)
  }
}