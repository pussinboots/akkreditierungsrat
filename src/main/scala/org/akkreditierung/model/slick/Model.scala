package org.akkreditierung.model.slick

import scala.slick.driver.H2Driver.simple._

import Database.threadLocalSession
import java.sql.{Timestamp, Date}
import java.security.MessageDigest
import java.util.{Date, Calendar}
import org.akkreditierung.DateUtil

case class Studiengang(var id: Option[Int] = None, jobId: Option[Int], fach: String, abschluss: String, hochschule: String, bezugstyp: String, link: Option[String], var gutachtentLink: Option[String] = None, updateDate: Option[Timestamp], var modifiedDate: Option[Timestamp], sourceId: Int) {
  lazy val checkSum = {
    val str = fach + abschluss + hochschule + bezugstyp + link.getOrElse("")
    MessageDigest.getInstance("MD5").digest(str.getBytes).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }
  lazy val attributes: Map[String, StudiengangAttribute] = (for {a <- StudiengangAttributes if a.id === id} yield (a.key->a)) toMap
}

case class StudiengangAttribute(var id: Int, key: String, value: String)

case class Job(id: Int, createDate: Timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis), newEntries: Int = 0, status: String = "started")

object Jobs extends Table[Job]("jobs") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def createDate = column[Timestamp]("createDate")
  def newEntries = column[Int]("newEntries")
  def status = column[String]("status")
  def * = id ~ createDate ~ newEntries~ status <> (Job, Job.unapply _)
  def forInsert = createDate ~ newEntries ~ status <> ({ t => Job(-1, t._1, t._2, t._3)}, { (u: Job) => Some((u.createDate, u.newEntries, u.status))}) returning id
  def insert(job: Job) = job.copy(id = forInsert.insert(job))
  def updateOrDelete(job: Job) {
    if (job.newEntries <= 0)
      Jobs.withFilter(_.id===job.id).delete
    else
      (for {jobs <- Jobs if jobs.id === job.id} yield (jobs.newEntries ~ jobs.status)).update(job.newEntries, job.status)
  }
  def findLatest() = Query(Jobs).sortBy(_.id.desc).take(1).firstOption
}

case class Source(id: Int, name: String, createDate: Timestamp = DateUtil.nowDateTime())

object Sources extends Table[Source]("sources") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def createDate = column[Timestamp]("createDate")
  def * = id ~  name ~ createDate <> (Source, Source.unapply _)
  def forInsert = name ~ createDate <> ({ t => Source(-1, name=t._1, createDate=t._2)}, { (u: Source) => Some((u.name, u.createDate))}) returning id
  def insert(source: Source) = source.copy(id = forInsert.insert(source))
}

object Studiengangs extends Table[Studiengang]("Studiengangs") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def jobId = column[Option[Int]]("jobId")
  def fach = column[String]("fach")
  def abschluss = column[String]("abschluss")
  def hochschule = column[String]("hochschule")
  def bezugstyp = column[String]("bezugstyp")
  def link = column[Option[String]]("link")
  def gutachtentLink = column[Option[String]]("gutachtentLink")
  def modifiedDate = column[Option[Timestamp]]("modifiedDate")
  def updateDate = column[Option[Timestamp]]("updateDate")
  def sourceId = column[Int]("sourceId")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = id.? ~ jobId ~ fach ~ abschluss ~ hochschule ~ bezugstyp ~ link~ gutachtentLink~ updateDate~ modifiedDate~ sourceId <> (Studiengang, Studiengang.unapply _)

  def findByFach(fach: String) =  (for {a <- Studiengangs if a.fach === fach} yield (a))
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
    val now = new Timestamp(Calendar.getInstance().getTimeInMillis)
    (Studiengangs.ddl ++ StudiengangAttributes.ddl).create
    Studiengangs.insert(Studiengang(Some(1), Some(1), "99 Market Street", "Groundsville", "CA", "95199", Some(""), Some(""), Some(now), None, 1))
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