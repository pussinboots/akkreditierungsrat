package org.akkreditierung.model.slick

import org.akkreditierung.model.DB
import DB.dal.profile.simple._
import scala.slick.lifted.{Query=>SlickQuery}
import scala.collection.mutable.Map

class QueryTapper[T,E](tapMe: SlickQuery[E,T]) {

  def cache(f: (SlickQuery[E,T]) => T): T = {
    val key = tapMe.selectStatement
    QueryTapper.cacheMap.get(tapMe.selectStatement).getOrElse{
      val r = f(tapMe)
      println("retrieved from database " + key)
      QueryTapper.cacheMap.put(key, r)
      r
    }.asInstanceOf[T]
  }
}
object QueryTapper {
  val cacheMap = Map[String, Any]()

  implicit def any2Tapper[E,T](toTap: SlickQuery[E,T]): QueryTapper[T,E] = new QueryTapper(toTap)
}
