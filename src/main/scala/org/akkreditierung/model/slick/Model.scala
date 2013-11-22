package org.akkreditierung.model.slick

import scala.slick.driver.H2Driver.simple._

import Database.threadLocalSession
import java.sql.Date
import java.security.MessageDigest

case class Studiengang(var id: Option[Int] = None, jobId: Option[Int], fach: String, abschluss: String, hochschule: String, bezugstyp: String, link: Option[String], var gutachtentLink: Option[String] = None, var modifiedDate: Option[Date], sourceId: Int) {
  lazy val checkSum = {
    val str = fach + abschluss + hochschule + bezugstyp + link.getOrElse("")
    MessageDigest.getInstance("MD5").digest(str.getBytes).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }
  lazy val attributes: Seq[StudiengangAttribute] = (for {a <- StudiengangAttributes if (a.id === id)} yield (a)).list
}

case class StudiengangAttribute(var id: Int, key: String, value: String)

object Studiengangs extends Table[Studiengang]("Studiengangs") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def jobId = column[Option[Int]]("jobId")
  def fach = column[String]("fach")
  def abschluss = column[String]("abschluss")
  def hochschule = column[String]("hochschule")
  def bezugstyp = column[String]("bezugstyp")
  def link = column[Option[String]]("link")
  def gutachtentLink = column[Option[String]]("gutachtentLink")
  def modifiedDate = column[Option[Date]]("modifiedDate")
  def sourceId = column[Int]("sourceId")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = id.? ~ jobId ~ fach ~ abschluss ~ hochschule ~ bezugstyp ~ link~ gutachtentLink~ modifiedDate~ sourceId <> (Studiengang, Studiengang.unapply _)
}

object StudiengangAttributes extends Table[StudiengangAttribute]("StudiengangAttributes") {
  def id = column[Int]("id")
  def key = column[String]("key")
  def value = column[String]("value")
  def * = id ~ key ~ value <> (StudiengangAttribute, StudiengangAttribute.unapply _)
  def studiengang = foreignKey("id", id, Studiengangs)(_.id)
}

object Test extends App {
  Database.forURL("jdbc:hsqldb:mem:test1;sql.enforce_size=false", driver = "org.hsqldb.jdbc.JDBCDriver") withSession {
    (Studiengangs.ddl ++ StudiengangAttributes.ddl).create
    Studiengangs.insert(Studiengang(Some(1), Some(1), "99 Market Street", "Groundsville", "CA", "95199", Some(""), Some(""), None, 1))
    StudiengangAttributes.insert(StudiengangAttribute(1, "99 Market Street", "Groundsville"))
    StudiengangAttributes.insert(StudiengangAttribute(1, "asdsad", "asdasd"))

    val implicitCrossJoin = for {
      s <- Studiengangs
    } yield (s)
    implicitCrossJoin foreach println
    println()
    implicitCrossJoin foreach(s=>println(s.attributes))
    println()
    val s = for {
      sa <- StudiengangAttributes
      s <- sa.studiengang
    } yield (sa, s)
    s foreach println
    println()
    val query = for(row <- StudiengangAttributes) yield row
    println(Query(query.length).first)
  }
}