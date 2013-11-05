package org.akkreditierung.ui.model

import com.avaje.ebean._
import java.io.Serializable

object DefaultBean {
  val DB_ADVERTISER_CONFIG: String = "akkreditierungsrat"
}
@SerialVersionUID(5517860393924994051L)
abstract class DefaultBean[T](dbName: String, entityClass: Class[T]) extends Serializable {
  def getQuery: Query[T] = Ebean.getServer(dbName).find(entityClass)

//  def getServer: EbeanServer = Ebean.getServer(dbName)
//
//  def getQuery(query: String): Query[T] = Ebean.getServer(dbName).createQuery(entityClass, query)
//
//  def getByPrimaryKey(primaryKey: AnyRef): T = getServer.find(entityClass, primaryKey)
//
//  def iteratAll(fetchSize: Int): QueryIterator[T] = getServer.find(entityClass).setBufferFetchSizeHint(fetchSize).findIterate
//
//  def countAll: Int = getServer.find(entityClass).findRowCount
//
//  def iteratAll: QueryIterator[T] = return iteratAll(25)

//  def update(entity: T, post: T => _ = postUpdate _) {
//    getServer.update(entity)
//    post(entity)
//  }
//
//  def delete(entity: T, post: T => _ = postDelete _) {
//    getServer.delete(entity)
//    post(entity)
//  }
//
//  def save(entity: T, post: T => _ = postSave _) {
//    getServer.save(entity)
//    post(entity)
//  }
//
//  def insert(entity: T, post: T => _ = postSave _) {
//    getServer.insert(entity)
//    post(entity)
//  }
//
//  def safeSave(entity: T) {
//    var transaction: Transaction = null
//    try {
//      transaction = getServer.beginTransaction
//      save(entity)
//      transaction.commit
//    }
//    catch {
//      case e: Exception => {
//        if (transaction != null) {
//          transaction.rollback
//        }
//        throw e
//      }
//    }
//  }

  def postDelete(deletedEntity: T) {}
  def postUpdate(updatedEntity: T) {}
  def postSave(savedEntity: T) {}
}