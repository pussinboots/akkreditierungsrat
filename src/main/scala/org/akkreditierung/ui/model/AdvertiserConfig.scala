package org.akkreditierung.ui.model

import javax.persistence._
import java.util.{Date, Map}
import scala.beans.BeanProperty
import org.akkreditierung.model.Studiengang

@Entity
@Table(name = "studiengaenge") class AdvertiserConfig {
  @Id @BeanProperty var id: Int = 0
  @BeanProperty var fach: String = null
  @BeanProperty @Column(name = "jobId") var jobId: Int = 0
  @BeanProperty var abschluss: String = null
  @BeanProperty var hochschule: String = null
  @BeanProperty var bezugstyp: String = null
  @BeanProperty var link: String = null
  @BeanProperty @Column(name = "GutachtenLink") var gutachtenLink: String = null
  @BeanProperty @Column(name = "modifiedDate") var modifiedDate: Date = null
  @BeanProperty @Column(name = "sourceId") var sourceId: Int = 0
  @OneToMany(cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY)
  @JoinColumn(name = "id", referencedColumnName = "id", nullable = true)
  @MapKey(name = "k")
  @BeanProperty var map: Map[String, StudiengaengeAttribute] = null

//  override def toString: String = {
//    "studiengaenge{" + "id=" + id + ", fach='" + fach + '}'
//  }

  def toStudienGang() = {
    Studiengang(jobId=Option(jobId), fach=fach, abschluss=abschluss, hochschule=hochschule, bezugstyp=bezugstyp, link=Option(link), gutachtentLink=Option(gutachtenLink), modifiedDate = Option(modifiedDate), sourceId = sourceId)
  }
}