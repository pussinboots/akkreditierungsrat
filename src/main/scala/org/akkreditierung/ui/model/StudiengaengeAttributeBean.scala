package org.akkreditierung.ui.model

class StudiengaengeAttributeBean extends DefaultBean[StudiengaengeAttribute](DefaultBean.DB_ADVERTISER_CONFIG, classOf[StudiengaengeAttribute]) {

  def findAll(id: Int) = getQuery.where().eq("id", id).findMap("k", classOf[String])
}