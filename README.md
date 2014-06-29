Example Project to show the integration of wicket with Scala and Slick
==================
[![Build Status](https://travis-ci.org/pussinboots/akkreditierungsrat.svg?branch=master)](https://travis-ci.org/pussinboots/akkreditierungsrat)
[![Unit Tests](https://unitcover.herokuapp.com/api/pussinboots/akkreditierungsrat/badge)](https://unitcover.herokuapp.com/#/builds/pussinboots/akkreditierungsrat/builds)

[![Unit Tests](http://unitcover.herokuapp.com/api/pussinboots/akkreditierungsrat/testsuites/badge?ts=1)](https://unitcover.herokuapp.com/#/builds/pussinboots/akkreditierungsrat/builds)

Live Demo - http://lit-ocean-2199.herokuapp.com/

This project is fully implemented with Scala and wicket. The Scala language make it 
much easier to use wicket as fronted framework and need less code compared with Java.

The first approach was implemented with Ebean as persistence framework, it is a very easy to use object
relation mapping framework for Java but is not so well suited for Scala. After a little of research to find a
good orm or persistence framework for Scala. The slick (Scala Language-Integrated Connection Kit) framework 
seems to be a good candidate because it is very well integreted with Scala. 

Wicket make it very easy to implements data table for html page based on database tables.


Here is the DataProvider implementation for wicket that use Ebean.
  
    object DefaultBean {
      val DB_ADVERTISER_CONFIG: String = "akkreditierungsrat"
    }
    @SerialVersionUID(5517860393924994051L)
    abstract class DefaultBean[T](dbName: String, entityClass: Class[T]) extends Serializable {
      def getQuery: Query[T] = Ebean.getServer(dbName).find(entityClass)
    }
  
    class GenericProvider[T](bean: DefaultBean[T]) extends SortableDataProvider[T, String] {
    
      def model(value: T): IModel[T] = new LoadableDetachableModel[T] { protected def load: T = value }
    
      private def orderBy(param: SortParam[String]): OrderBy[T] = {
        val orderBy: OrderBy[T] = new OrderBy[T]
        if (param.isAscending) orderBy.asc(param.getProperty) else orderBy.desc(param.getProperty)
        orderBy
      }
    
      def filter(query: Query[T]): Query[T] = query
    
      def iterator(first: Long, count: Long): Iterator[T] = filter(bean.getQuery).setOrderBy(orderBy(getSort)).setMaxRows(count.toInt).setFirstRow(first.toInt).findList().iterator
    
      def size: Long = filter(bean.getQuery).findRowCount
    }

Here is the DataProvider implementation for wicket that use Slick.

    class GenericSlickProvider[E <: Table[T], T](query: SlickQuery[E, T]) extends SortableDataProvider[T, String] {
    
      def model(value: T): IModel[T] = new LoadableDetachableModel[T] { protected def load: T = value }
    
      def filter(query: SlickQuery[E, T]): SlickQuery[E, T] = query
    
      def paging(query: SlickQuery[E, T], first: Long, count: Long) = filter(query).drop(first.toInt).take(count.toInt)
      import collection.JavaConversions._
      def iterator(first: Long, count: Long): java.util.Iterator[T] = DB.db withSession paging(query, first, count).sortBy(sortKey(_, getSort)).list.iterator
    
      def size: Long = DB.db withSession SlickQuery(filter(query).length).first
    
      def sortKey[T](e: E, param: SortParam[String]): ColumnOrdered[_] = param.isAscending match {
        case true => e.column[String](param.getProperty).asc
        case false => e.column[String](param.getProperty).desc
      }
    }

The migration from Ebean to Slick was more or less easy. The complicated part was to implement dynamic sorting 
at runtime based on the column name from wicket. The Documentation of Slick was not very helpful after a while 
of searching the world wide web i got a hint how to do that.

    sortBy(sortKey(_, getSort))
    e.column[String](param.getProperty).asc
    
Above the solution for dynamic sorting is summarized.
