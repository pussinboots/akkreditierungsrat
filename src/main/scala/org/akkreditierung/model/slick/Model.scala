package org.akkreditierung.model.slick

import scala.slick.driver.{JdbcProfile, H2Driver, MySQLDriver}
import java.sql.{Timestamp, Date}
import java.security.MessageDigest
import java.util.{Date, Calendar}
import org.akkreditierung.DateUtil
import org.akkreditierung.model.DB
import scala.beans.{BeanInfo, BeanProperty}

trait Profile {
  val profile: JdbcProfile
}

object SourceAkkreditierungsRat {
  val name = "akkreditierungsrat"
}
/**
 * The Data Access Layer contains all components and a profile
 */
class DAL(override val profile: JdbcProfile) extends StudiengangComponent with StudiengangAttributesComponent with JobsComponent with SourcesComponent with Profile {
  import profile.simple._
  def recreate(implicit session: Session) = {
    drop(session)
    create(session)
  }
  def create(implicit session: Session) = (studiengangs.ddl ++ studiengangAttributes.ddl ++ sources.ddl ++ jobs.ddl).create //helper method to create all tables

  def drop(implicit session: Session) {
    try{
      (studiengangs.ddl ++ studiengangAttributes.ddl ++ sources.ddl ++ jobs.ddl).drop
    } catch {
      case ioe: Exception =>
    }
  }
}

case class Studiengang(var id: Option[Int] = None, jobId: Option[Int], fach: String, abschluss: String, hochschule: String,
                       bezugstyp: String, link: Option[String], var gutachtenLink: Option[String] = None, updateDate: Option[Timestamp],
                       var modifiedDate: Option[Timestamp], sourceId: Int, checkSum: String) {

  import org.akkreditierung.model.DB.dal._
  import DB.dal.profile.simple._
  import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
  lazy val attributes: Map[String, StudiengangAttribute] = {
    (for {a <- studiengangAttributes if a.id === id.get} yield (a.key->a)) toMap
  }
}

object Studiengang {
  def calculateCheckSum( fach: String, abschluss: String, hochschule: String,
                       bezugstyp: String, link: Option[String]) = {
	val str = fach + abschluss + hochschule + bezugstyp + link.getOrElse("")
	MessageDigest.getInstance("MD5").digest(str.getBytes).map(0xFF & _).map {
	"%02x".format(_)
	}.foldLeft("") {
	_ + _
	}
  }
  def apply(id: Option[Int], jobId: Option[Int], fach: String, abschluss: String, hochschule: String,
                       bezugstyp: String, link: Option[String], updateDate: Option[Timestamp],           modifiedDate: Option[Timestamp], sourceId: Int) = new Studiengang(id, jobId, fach,     
			abschluss, hochschule, bezugstyp, link, None, updateDate, modifiedDate, sourceId, 		
			checkSum=calculateCheckSum(fach, abschluss, hochschule, bezugstyp, link))

  def apply(id: Option[Int], jobId: Option[Int], fach: String, abschluss: String, hochschule: String,
                       bezugstyp: String, link: Option[String], gutachtenLink: Option[String], updateDate: Option[Timestamp],           modifiedDate: Option[Timestamp], sourceId: Int) = new Studiengang(id, jobId, fach,     
			abschluss, hochschule, bezugstyp, link, gutachtenLink, updateDate, modifiedDate, sourceId, 		
			checkSum=calculateCheckSum(fach, abschluss, hochschule, bezugstyp, link))
}

object StudiengangC {
  def neu() = Studiengang(Some(0),Some(0),"","","","",Some(""), None, DateUtil.nowDateTimeOpt(),1)
}

trait StudiengangComponent { this: Profile with StudiengangAttributesComponent=> //requires a Profile to be mixed in...
  import profile.simple._ //...to be able import profile.simple._
  //import profile.simple.Database.threadLocalSession

  class Studiengangs(tag: Tag) extends Table[Studiengang](tag, "studiengaenge") {
    val createStudiengang = Studiengang.apply(_: Option[Int], _: Option[Int], _: String, _: String, _: String,
                       _: String, _: Option[String], _: Option[String], _: Option[Timestamp],_: Option[Timestamp], _: Int, _: String)     
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def jobId = column[Option[Int]]("jobId")
    def fach = column[String]("fach")
    def abschluss = column[String]("abschluss")
    def hochschule = column[String]("hochschule")
    def bezugstyp = column[String]("bezugstyp")
    def link = column[Option[String]]("link")
    def gutachtentLink = column[Option[String]]("GutachtenLink")
    def modifiedDate = column[Option[Timestamp]]("modifiedDate")
    def updateDate = column[Option[Timestamp]]("updateDate")
    def checkSum = column[String]("checksum")
    def uniqueCheckSumDB = index("IDX_CHECKSUM", checkSum, unique = true)
    def sourceId = column[Int]("sourceId")
    def * = (id.?, jobId, fach, abschluss, hochschule, bezugstyp, link, gutachtentLink, updateDate, modifiedDate, sourceId, checkSum) <> (createStudiengang.tupled, Studiengang.unapply)
  }
  val studiengangs = TableQuery[Studiengangs]
  val studienGangForInsert = studiengangs returning studiengangs.map(_.id) into { case (s, id) => s.copy(id = Some(id)) }
  def studienGanginsert(studiengang: Studiengang)(implicit session: Session) = studienGangForInsert.insert(studiengang)
  def findByFach(fach: String)(implicit session: Session) =  (for {a <- studiengangs if a.fach === fach} yield (a))
  def findAllStudienGangs()(implicit session: Session) = for {a <- studiengangs} yield (a)
}

case class StudiengangAttribute(var id: Int, key: String, value: String)
trait StudiengangAttributesComponent { this: Profile with StudiengangComponent=> //requires a Profile to be mixed in...
  import profile.simple._
  //import profile.simple.Database.threadLocalSession
  class StudiengangAttributes(tag: Tag) extends Table[StudiengangAttribute](tag, "studiengaenge_attribute") {
    def id = column[Int]("id")
    def key = column[String]("k")
    def value = column[String]("v")
    def pk = primaryKey("pk_a", (id, key, value))
    def studiengang = foreignKey("id", id, studiengangs)(_.id)
    def * = (id, key, value) <> (StudiengangAttribute.tupled, StudiengangAttribute.unapply)
  }
  val studiengangAttributes = TableQuery[StudiengangAttributes]
  def findAllStudiengangAttributes()(implicit session: Session) =  (for {a <- studiengangAttributes} yield (a))
}

object StudiengangAttributeC {
  def neu(field: String) = new StudiengangAttribute(0,field,"")
}

case class Job(id: Int, createDate: Timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis), newEntries: Int = 0, status: String = "started")
trait JobsComponent { this: Profile => //requires a Profile to be mixed in...
  import profile.simple._
  import profile.simple.Database.dynamicSession
  class Jobs(tag: Tag) extends Table[Job](tag,"jobs"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def createDate = column[Timestamp]("createDate")
    def newEntries = column[Int]("newEntries")
    def status = column[String]("status")
    def * = (id, createDate, newEntries, status) <> (Job.tupled, Job.unapply)
  }
  val jobs = TableQuery[Jobs]
  val jobForInsert = jobs returning jobs.map(_.id) into { case (s, id) => s.copy(id = id) }
  def jobInsert(job: Job)(implicit session: Session) = jobForInsert.insert(job)
  def updateOrDelete(job: Job)(implicit session: Session) = if (job.newEntries <= 0) jobs.filter(_.id===job.id).delete else (for {jobsdb <- jobs if jobsdb.id === job.id} yield (jobsdb.newEntries, jobsdb.status)).update(job.newEntries, job.status)
  def findLatestJob(): Option[Job] = jobs.sortBy(_.id.desc).take(1).firstOption
}

case class Source(id: Int =0, name: String, createDate: Timestamp = DateUtil.nowDateTime())
trait SourcesComponent { this: Profile => //requires a Profile to be mixed in...
  import profile.simple._
  //import profile.simple.Database.threadLocalSession
  class Sources(tag: Tag) extends Table[Source](tag,"sources") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def createDate = column[Timestamp]("createDate")
    def * = (id, name, createDate) <> (Source.tupled, Source.unapply)
  }
  val sources = TableQuery[Sources]
  val sourceForInsert = sources returning sources.map(_.id) into { case (s, id) => s.copy(id = id) }
  def sourceInsert(source: Source)(implicit session: Session) = sourceForInsert.insert(source)
  def findOrInsert(name: String)(implicit session: Session): Option[Source] = sources.filter(_.name===name).take(1).firstOption.orElse(Option(sourceInsert(Source(0, name))))

}

//object Test extends App {
//  Database.forURL("jdbc:hsqldb:mem:test1;sql.enforce_size=false", driver = "org.hsqldb.jdbc.JDBCDriver") withSession {
//    val now = new Timestamp(Calendar.getInstance().getTimeInMillis)
//    (Studiengangs.ddl ++ StudiengangAttributes.ddl).create
//    Studiengangs.insert(Studiengang(Some(1), Some(1), "99 Market Street", "Groundsville", "CA", "95199", Some(""), Some(""), Some(now), None, 1))
//    StudiengangAttributes.insert(StudiengangAttribute(1, "99 Market Street", "Groundsville"))
//    StudiengangAttributes.insert(StudiengangAttribute(1, "asdsad", "asdasd"))
//
//    val implicitCrossJoin = for {
//      s <- Studiengangs
//    } yield (s)
//    implicitCrossJoin foreach println
//    println()
//    implicitCrossJoin foreach(s=>println(s.attributes))
//    println()
//    val s = for {
//      sa <- StudiengangAttributes
//      s <- sa.studiengang
//    } yield (sa, s)
//    s foreach println
//    println()
//    val query = for(row <- StudiengangAttributes) yield row
//    println(Query(query.length).first)
//  }
//}
