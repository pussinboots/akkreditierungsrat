package org.akkreditierung.ui.model

class JobBean extends DefaultBean[Job](DefaultBean.DB_ADVERTISER_CONFIG, classOf[Job]) {
  def findLatest() = {
    Option(getQuery.orderBy().desc("id").setMaxRows(1).findUnique()).getOrElse(new Job())
  }
}