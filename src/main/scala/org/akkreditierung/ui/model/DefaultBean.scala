package org.akkreditierung.ui.model

import com.avaje.ebean._
import java.io.Serializable
object DefaultBean {
  val DB_ADVERTISER_CONFIG: String = "akkreditierungsrat"
}
abstract class DefaultBean[T] extends Serializable {

  private final val serialVersionUID: Long = 5517860393924994051L

  object Change extends Enumeration {
    type WeekDay = Value
    val INSERT = Value("Mon")
    val UPDATE = Value("Mon")
    val SAVE = Value("Mon")
    val DELETE = Value("Tue")
  }

  /**
   * Default constructor.
   *
   * @param dbName
     * the name of the database to use (see ebean.properties)
   * @param entityClass
     * the class of the entity ( determinate the sql table to use)
   */
  def this(dbName: String, entityClass: Class[T]) {
    this()
    this.dbName = dbName
    this.entityClass = entityClass
  }

  def getServer: EbeanServer = {
    return Ebean.getServer(dbName)
  }

  def getQuery: Query[T] = {
    return Ebean.getServer(dbName).find(entityClass)
  }

  def getQuery(query: String): Query[T] = {
    return Ebean.getServer(dbName).createQuery(entityClass, query)
  }

  def getByPrimaryKey(primaryKey: AnyRef): T = {
    return getServer.find(entityClass, primaryKey)
  }

  def iteratAll(fetchSize: Int): QueryIterator[T] = {
    return getServer.find(entityClass).setBufferFetchSizeHint(fetchSize).findIterate
  }

  def countAll: Int = {
    return getServer.find(entityClass).findRowCount
  }

  def iteratAll: QueryIterator[T] = {
    return iteratAll(25)
  }

  def update(entity: T) {
    getServer.update(entity)
    postChange(Change.UPDATE, entity)
  }

  def delete(entity: T) {
    getServer.delete(entity)
    postChange(Change.DELETE, entity)
  }

  def save(entity: T) {
    getServer.save(entity)
    postChange(Change.SAVE, entity)
  }

  def insert(entity: T) {
    getServer.insert(entity)
    postChange(Change.INSERT, entity)
  }

  def safeSave(entity: T) {
    var transaction: Transaction = null
    try {
      transaction = getServer.beginTransaction
      getServer.save(entity)
      postChange(Change.SAVE, entity)
      transaction.commit
    }
    catch {
      case e: Exception => {
        if (transaction != null) {
          transaction.rollback
        }
        throw e
      }
    }
  }

  private def postChange(changeEvent: Change.WeekDay, changedEntity: T) {
    postChanged
    changeEvent match {
      case Change.SAVE =>
        postSave(changedEntity)
        return
      case Change.DELETE =>
        postDelete(changedEntity)
        return
      case Change.UPDATE =>
        postUpdate(changedEntity)
        return
      case _ =>
        return //todo: break is not supported
    }
  }

  def postChanged {
  }

  private def postDelete(deletedEntity: T) {
  }

  private def postUpdate(updatedEntity: T) {
  }

  def postSave(savedEntity: T) {
  }

  private var dbName: String = null
  private var entityClass: Class[T] = null
}