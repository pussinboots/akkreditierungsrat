package org.akkreditierung.model

import anorm._
import java.security.MessageDigest
import anorm.SqlParser._
import anorm.~

case class Studiengang(var id: Option[Int] = None, fach: String, abschluss: String, hochschule: String, bezugstyp: String, link: String) {
  lazy val checkSum = {
    val str = fach + abschluss + hochschule + bezugstyp + link
    MessageDigest.getInstance("MD5").digest(str.getBytes).map(0xFF & _).map {"%02x".format(_)}.foldLeft("") {_ + _}
  }
}

case class StudiengangAttribute(var id:Int, key: String, value: String)

object StudiengangAttribute{
  val single = {
      get[Int]("studiengaenge_attribute.id") ~
      get[String]("studiengaenge_attribute.k") ~
      get[String]("studiengaenge_attribute.v") map {
      case id ~ k ~ v => StudiengangAttribute(id, k, v)
    }
  }

  def findAll(): Seq[StudiengangAttribute] = { DB.withConnection { implicit connection =>
      SQL("select * from studiengaenge_attribute").as(StudiengangAttribute.single *)
    }
  }

  def Insert(studiengangAttribute: StudiengangAttribute): StudiengangAttribute = { DB.withConnection { implicit connection =>
      SQL("insert into studiengaenge_attribute (id, k, v) values ({id},{key},{value})").on(
        'id -> studiengangAttribute.id,
        'key -> studiengangAttribute.key,
        'value -> studiengangAttribute.value).executeInsert()
    }
    studiengangAttribute
  }
}

object Studiengang {
  val single = {
    get[Int]("studiengaenge.id") ~
      get[String]("studiengaenge.fach") ~
      get[String]("studiengaenge.abschluss") ~
      get[String]("studiengaenge.hochschule") ~
      get[String]("studiengaenge.bezugstyp") ~
      get[String]("studiengaenge.link") map {
      case id ~ fach ~ abschluss ~ hochschule ~ bezugstyp ~ link => Studiengang(Option(id), fach, abschluss, hochschule, bezugstyp, link)
    }
  }

  def Insert(studiengang: Studiengang): Studiengang = { DB.withConnection { implicit connection =>
      SQL("insert into studiengaenge (fach, abschluss, hochschule, bezugstyp, link, checksum) values ({fach},{abschluss},{hochschule},{bezugstyp},{link},{checksum})").on(
        'fach -> studiengang.fach,
        'abschluss -> studiengang.abschluss,
        'hochschule -> studiengang.hochschule,
        'bezugstyp -> studiengang.bezugstyp,
        'link -> studiengang.link,
        'checksum -> studiengang.checkSum).executeInsert()
    } match { case Some(long: Long) =>
        studiengang.id = Option(long.toInt) // The Primary Key
    }
    studiengang
  }

  def findAll(): Seq[Studiengang] = { DB.withConnection { implicit connection =>
      SQL("select * from studiengaenge").as(Studiengang.single *)
    }
  }

  //only work in hsqldb
  def Merge(studiengang: Studiengang): Studiengang = { DB.withConnection { implicit connection =>
      SQL(
        """MERGE INTO studiengaenge as t USING (VALUES({fach},{abschluss},{hochschule},{bezugstyp},{link},{checksum}))
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