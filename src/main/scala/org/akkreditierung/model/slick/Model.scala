package org.akkreditierung.model.slick

import scala.slick.driver.{ExtendedProfile, H2Driver, MySQLDriver}
import java.sql.{Timestamp, Date}
import java.security.MessageDigest
import java.util.{Date, Calendar}
import org.akkreditierung.DateUtil
import scala.slick.session.{Database, Session}
import org.akkreditierung.model.DB
import com.avaje.ebean.Expr

trait Profile {
  val profile: ExtendedProfile
}
/**
 * The Data Access Layer contains all components and a profile
 */
class DAL(override val profile: ExtendedProfile) extends StudiengangComponent with StudiengangAttributesComponent with JobsComponent with SourcesComponent with Profile {
  import profile.simple._
  def create(implicit session: Session)  {
    (Studiengangs.ddl ++ StudiengangAttributes.ddl ++ Sources.ddl ++ Jobs.ddl).create //helper method to create all tables
  }

  def drop(implicit session: Session) {
    try{
      (Studiengangs.ddl ++ StudiengangAttributes.ddl ++ Sources.ddl ++ Jobs.ddl).drop
    } catch {
      case ioe: Exception =>
    }
  }
}

case class Studiengang(var id: Option[Int] = None, jobId: Option[Int], fach: String, abschluss: String, hochschule: String,
                       bezugstyp: String, link: Option[String], var gutachtentLink: Option[String] = None, updateDate: Option[Timestamp],
                       var modifiedDate: Option[Timestamp], sourceId: Int) {
  lazy val checkSum = {
    val str = fach + abschluss + hochschule + bezugstyp + link.getOrElse("")
    MessageDigest.getInstance("MD5").digest(str.getBytes).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }
  import org.akkreditierung.model.DB.dal._
  import DB.dal.profile.simple._
  import DB.dal.profile.simple.Database.threadLocalSession
  lazy val attributes: Map[String, StudiengangAttribute] = {
    println("attribues: " + (for {a <- StudiengangAttributes if a.id === id.get} yield (a.key->a)).selectStatement)
    (for {a <- StudiengangAttributes if a.id === id.get} yield (a.key->a)) toMap
  }
}

trait StudiengangComponent { this: Profile with StudiengangAttributesComponent=> //requires a Profile to be mixed in...
  import profile.simple._ //...to be able import profile.simple._
  import profile.simple.Database.threadLocalSession

  object Studiengangs extends Table[Studiengang]("studiengaenge") {
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
    def sourceId = column[Int]("sourceId")
    def * = id.? ~ jobId ~ fach ~ abschluss ~ hochschule ~ bezugstyp ~ link~ gutachtentLink~ updateDate~ modifiedDate~ sourceId <> (Studiengang, Studiengang.unapply _)
    def forInsert = jobId ~ fach ~ abschluss ~ hochschule ~ bezugstyp ~ link~ gutachtentLink~ updateDate~ modifiedDate~ sourceId <> ({ t => Studiengang(None,t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10)}, {(u: Studiengang) => Some((u.jobId, u.fach, u.abschluss, u.hochschule, u.bezugstyp, u.link, u.gutachtentLink, u.updateDate, u.modifiedDate, u.sourceId))}) returning id
    def insert(studiengang: Studiengang) = studiengang.copy(id = Some(forInsert.insert(studiengang)))
    def findByFach(fach: String) =  (for {a <- Studiengangs if a.fach === fach} yield (a))
    def columns() = Map("id"->id,"jobId"->jobId,"fach"->fach,"abschluss"->abschluss,"hochschule"->hochschule,"bezugstyp"->bezugstyp,"link"->link,"gutachtentLink"->gutachtentLink,"updateDate"->updateDate,"modifiedDate"->modifiedDate,"sourceId"->sourceId)

    def findByAttribute(attribute: String, value:String) = {
      Studiengangs.flatMap{c=>
        StudiengangAttributes.filter(s => s.id === c.id)
      }
    }
  }
}

case class StudiengangAttribute(var id: Int, key: String, value: String)
trait StudiengangAttributesComponent { this: Profile with StudiengangComponent=> //requires a Profile to be mixed in...
  import profile.simple._
  import profile.simple.Database.threadLocalSession
  object StudiengangAttributes extends Table[StudiengangAttribute]("studiengaenge_attribute") {
    def id = column[Int]("id")
    def key = column[String]("k")
    def value = column[String]("v")
    def * = id ~ key ~ value <> (StudiengangAttribute, StudiengangAttribute.unapply _)
    def studiengang = foreignKey("id", id, Studiengangs)(_.id)
  }
}

case class Job(id: Int, createDate: Timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis), newEntries: Int = 0, status: String = "started")
trait JobsComponent { this: Profile => //requires a Profile to be mixed in...
  import profile.simple._
  import profile.simple.Database.threadLocalSession
  object Jobs extends Table[Job]("jobs"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def createDate = column[Timestamp]("createDate")
    def newEntries = column[Int]("newEntries")
    def status = column[String]("status")
    def * = id ~ createDate ~ newEntries~ status <> (Job, Job.unapply _)
    def forInsert = createDate ~ newEntries ~ status <> ({ t => Job(-1, t._1, t._2, t._3)}, { (u: Job) => Some((u.createDate, u.newEntries, u.status))}) returning id
    def insert(job: Job) = job.copy(id = forInsert.insert(job))
    def updateOrDelete(job: Job) = if (job.newEntries <= 0) Query(Jobs).filter(_.id===job.id).delete else (for {jobs <- Jobs if jobs.id === job.id} yield (jobs.newEntries ~ jobs.status)).update(job.newEntries, job.status)
    def columns() = Map("id"->id)
    def findLatest(): Option[Job] = {
      println(Query(Jobs).sortBy{value=>
        value.id.desc
      }.take(1).selectStatement)
      Query(Jobs).sortBy(_.id.desc).take(1).firstOption
    }
  }
}

case class Source(id: Int =0, name: String, createDate: Timestamp = DateUtil.nowDateTime())
trait SourcesComponent { this: Profile => //requires a Profile to be mixed in...
  import profile.simple._
  import profile.simple.Database.threadLocalSession
  object Sources extends Table[Source]("sources") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def createDate = column[Timestamp]("createDate")
    def * = id ~  name ~ createDate <> (Source, Source.unapply _)
    def forInsert = name ~ createDate <> ({ t => Source(-1, name=t._1, createDate=t._2)}, { (u: Source) => Some((u.name, u.createDate))}) returning id
    def insert(source: Source) = source.copy(id = forInsert.insert(source))
    def findOrInsert(name: String): Option[Source] = Query(Sources).filter(_.name===name).take(1).firstOption.orElse(Option(insert(Source(0, name))))
  }
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