package org.akkreditierung.model

import anorm._
import java.security.MessageDigest
import anorm.SqlParser._
import anorm.~
import java.util.Date

object SourceAkkreditierungsRat {
  val name = "akkreditierungsrat"
}

//TODO Datum erfassung (eventuell datum änderung)
case class Studiengang(var id: Option[Int] = None, jobId: Option[Int], fach: String, abschluss: String, hochschule: String, bezugstyp: String, link: String, var gutachtentLink: Option[String] = None, sourceId: Int) {
  lazy val checkSum = {
    val str = fach + abschluss + hochschule + bezugstyp + link
    MessageDigest.getInstance("MD5").digest(str.getBytes).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }
}

case class StudiengangAttribute(var id: Int, key: String, value: String)

trait DBBean[T] {
  def Count(): Long

  def Find(maxRows: Long, firstRow: Long): Seq[T]
}

case class Job(var id: Option[Int] = None, createDate: Date = new Date(), newEntries: Int = 0, status: String = "started")

case class Source(var id: Option[Int] = None, name: String, createDate: Date = new Date())

object StudiengangAttribute extends DBBean[StudiengangAttribute] {
  val single = {
    get[Int]("studiengaenge_attribute.id") ~
      get[String]("studiengaenge_attribute.k") ~
      get[String]("studiengaenge_attribute.v") map {
      case id ~ k ~ v => StudiengangAttribute(id, k, v)
    }
  }

  def Count() = {
    DB.withConnection {
      implicit connection =>
        SQL("select count(*) from studiengaenge_attribute").as(scalar[Long].single)
    }
  }

  def Find(maxRows: Long, firstRow: Long) = {
    val onList = Seq(
      Some('limit -> maxRows),
      Some('offset -> firstRow)
    ).flatMap(_.map(v => v._1 -> toParameterValue(v._2)))
    DB.withConnection {
      implicit connection =>
        SQL("select * from studiengaenge_attribute LIMIT {limit} OFFSET {offset}").on(
          onList: _*
        ).as(StudiengangAttribute.single *)
    }
  }

  def Find[A](maxRows: Long, firstRow: Long, where: Map[String, ParameterValue[A]]) = {
    val onList = Seq(
      Some('limit -> maxRows),
      Some('offset -> firstRow)
    ).flatMap(_.map(v => v._1 -> toParameterValue(v._2)))
    where.map {
      x => s"${x._1} = {${x._1}}"
    }.mkString(" AND ")
    DB.withConnection {
      implicit connection =>
        SQL("select * from studiengaenge_attribute LIMIT {limit} OFFSET {offset}").on(
          onList: _*
        ).as(StudiengangAttribute.single *)
    }
  }


  def findAll(): Seq[StudiengangAttribute] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from studiengaenge_attribute").as(StudiengangAttribute.single *)
    }
  }

  def Insert(studiengangAttribute: StudiengangAttribute) = {
    DB.withConnection {
      implicit connection =>
        SQL("insert into studiengaenge_attribute (id, k, v) values ({id},{key},{value})").on(
          'id -> studiengangAttribute.id,
          'key -> studiengangAttribute.key,
          'value -> studiengangAttribute.value).executeInsert()
    }
    studiengangAttribute
  }

  def Inserts(studiengangAttributes: Seq[StudiengangAttribute]) = {
    studiengangAttributes.foreach(s => Insert(s))
    studiengangAttributes
  }
}

object Studiengang {
  val single = {
    get[Int]("studiengaenge.id") ~
      get[Option[Int]]("studiengaenge.jobId") ~
      get[String]("studiengaenge.fach") ~
      get[String]("studiengaenge.abschluss") ~
      get[String]("studiengaenge.hochschule") ~
      get[String]("studiengaenge.bezugstyp") ~
      get[String]("studiengaenge.link") ~
      get[Option[String]]("studiengaenge.GutachtenLink") ~
      get[Int]("studiengaenge.sourceId") map {
      case id ~ jobId ~ fach ~ abschluss ~ hochschule ~ bezugstyp ~ link ~ gutachtenLink ~ sourceId => Studiengang(Option(id), jobId, fach, abschluss, hochschule, bezugstyp, link, gutachtenLink, sourceId)
    }
  }

  def Insert(studiengang: Studiengang) = {
    DB.withConnection {
      implicit connection =>
        SQL("insert into studiengaenge (jobId, fach, abschluss, hochschule, bezugstyp, link, checksum, sourceId) values ({jobId}, {fach},{abschluss},{hochschule},{bezugstyp},{link},{checksum},{sourceId})").on(
          'fach -> studiengang.fach,
          'jobId -> studiengang.jobId,
          'abschluss -> studiengang.abschluss,
          'hochschule -> studiengang.hochschule,
          'bezugstyp -> studiengang.bezugstyp,
          'link -> studiengang.link,
          'checksum -> studiengang.checkSum,
          'sourceId -> studiengang.sourceId).executeInsert()
    } match {
      case Some(long: Long) =>
        studiengang.id = Option(long.toInt) // The Primary Key
    }
    studiengang
  }

  def Inserts(studiengang: Seq[Studiengang]) = {
    studiengang.foreach(s => Insert(s))
    studiengang
  }

  def UpdateGutachtentLink(studiengang: Studiengang) = {
    DB.withConnection {
      implicit connection =>
        SQL("update studiengaenge set GutachtenLink={gutachtenLink} where id={id}").on(
          'id -> studiengang.id,
          'gutachtenLink -> studiengang.gutachtentLink).executeInsert()
    }
    studiengang
  }

  def findAll(): Seq[Studiengang] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from studiengaenge").as(Studiengang.single *)
    }
  }

  def findByFach(fach: String): Studiengang = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from studiengaenge where fach={fach}").on(
          'fach -> fach).single(Studiengang.single)
    }
  }

  //only work in hsqldb
  def Merge(studiengang: Studiengang) = {
    DB.withConnection {
      implicit connection =>
        SQL( """MERGE INTO studiengaenge as t USING (VALUES({fach},{abschluss},{hochschule},{bezugstyp},{link},{checksum}))
             AS vals(fach, abschluss, hochschule, bezugstyp, link, checksum) ON t.checksum = vals.checksum
             WHEN NOT MATCHED THEN INSERT (fach, abschluss, hochschule, bezugstyp, link, checksum) VALUES vals.fach, vals.abschluss, vals.hochschule, vals.bezugstyp, vals.link, svals.checksum
             """.stripMargin).on(
          'fach -> studiengang.fach,
          'abschluss -> studiengang.abschluss,
          'hochschule -> studiengang.hochschule,
          'bezugstyp -> studiengang.bezugstyp,
          'link -> studiengang.link,
          'checksum -> studiengang.checkSum).executeInsert()
    }
    studiengang
  }
}

object Job {
  val single = {
    get[Int]("jobs.id") ~
      get[Date]("jobs.createDate") ~
      get[Int]("jobs.newEntries") ~
      get[String]("jobs.status") map {
      case id ~ createDate ~ newEntries ~ status => Job(Option(id), createDate, newEntries, status)
    }
  }

  def Insert(job: Job) = {
    DB.withConnection {
      implicit connection =>
        SQL("insert into jobs (newEntries, status) values ({newEntries}, {status})").on(
          'newEntries -> job.newEntries,
          'status -> job.status).executeInsert()
    } match {
      case Some(long: Long) =>
        job.id = Option(long.toInt) // The Primary Key
    }
    job
  }

  def UpdateOrDelete(job: Job) {
    if (job.newEntries > 0) {
      Update(job)
    } else {
      Delete(job)
    }
  }

  def Update(job: Job) = {
    DB.withConnection {
      implicit connection =>
        SQL("update jobs set newEntries={newEntries}, status={status} where id={id}").on(
          'id -> job.id,
          'newEntries -> job.newEntries,
          'status -> job.status).executeInsert()
    }
    job
  }

  def Delete(job: Job) {
    DB.withConnection {
      implicit connection =>
        SQL("delete from jobs where id={id}").on(
          'id -> job.id).executeInsert()
    }
  }

  def findAll(): Seq[Job] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from jobs").as(Job.single *)
    }
  }

  def findLatest(): Option[Job] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from jobs order by createDate desc limit 1").singleOpt(Job.single)
    }
  }
}

object Source {
  val single = {
    get[Int]("sources.id") ~
      get[String]("sources.name") ~
      get[Date]("sources.createDate") map {
      case id ~ name ~ createDate => Source(Option(id), name, createDate)
    }
  }

  def Insert(source: Source) = {
    DB.withConnection {
      implicit connection =>
        SQL("insert into sources (name, createDate) values ({name}, {createDate})").on(
          'name -> source.name,
          'createDate -> source.createDate).executeInsert()
    } match {
      case Some(long: Long) =>
        source.id = Option(long.toInt) // The Primary Key
    }
    source
  }

  def FindAll(): Seq[Source] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from sources").as(Source.single *)
    }
  }

  def FindByName(name: String): Option[Source] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from sources where name={name}").on(
        'name -> name
        ).singleOpt(Source.single)
    }
  }

  def FindOrCreateSourceAkkreditierungsrat(): Option[Source] = {
    DB.withConnection {
      implicit connection =>
        FindByName(SourceAkkreditierungsRat.name).orElse(
          Option(Insert(Source(name=SourceAkkreditierungsRat.name)))
        )
    }
  }

  def Delete(source: Source) {
    DB.withConnection {
      implicit connection =>
        SQL("delete from sources where id={id}").on(
          'id -> source.id.get).executeInsert()
    }
  }
}