package org.akkreditierung.ui.model

import com.avaje.ebean._
import java.io.Serializable

object DefaultBean {
  val DB_ADVERTISER_CONFIG: String = "akkreditierungsrat"
}

@SerialVersionUID(5517860393924994051L)
abstract class DefaultBean[T](dbName: String, entityClass: Class[T]) extends Serializable {
  def getQuery: Query[T] = Ebean.getServer(dbName).find(entityClass)
}