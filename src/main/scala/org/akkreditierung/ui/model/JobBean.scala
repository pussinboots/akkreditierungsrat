package org.akkreditierung.ui.model

class JobBean extends DefaultBean[Job](DefaultBean.DB_ADVERTISER_CONFIG, classOf[Job]) {
  def findLatest() = {
    val job = getQuery.orderBy().desc("status").setMaxRows(1).findUnique()
    if (job==null) {
      new Job()
    } else {
      job
    }
  }
}