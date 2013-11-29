package org.akkreditierung.model

package object slick {
  import org.akkreditierung.model.DB
  import DB.dal._
  import DB.dal.profile.simple._
  import scala.slick.session.Database
  import Database.threadLocalSession
}
