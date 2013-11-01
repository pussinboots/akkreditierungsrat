package org.akkreditierung.ui.model

import javax.persistence._
import java.io.Serializable
import java.util.Map
import scala.beans.BeanProperty
import org.akkreditierung.model.Studiengang

@Entity
@Table(name = "studiengaenge") class AdvertiserConfig {
  @Id @BeanProperty var id: Int = 0
  @BeanProperty var fach: String = null
  @BeanProperty var abschluss: String = null
  @BeanProperty var hochschule: String = null
  @BeanProperty var bezugstyp: String = null
  @BeanProperty var link: String = null
  @BeanProperty @Column(name = "GutachtenLink") var gutachtenLink: String = null
  @OneToMany(cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY)
  @JoinColumn(name = "id", referencedColumnName = "id", nullable = true)
  @MapKey(name = "k")
  @BeanProperty var map: Map[String, StudiengaengeAttribute] = null

  override def toString: String = {
    "studiengaenge{" + "id=" + id + ", fach='" + fach + '}'
  }

  def toStudienGang() = {
    Studiengang(fach=fach, abschluss=abschluss, hochschule=hochschule, bezugstyp=bezugstyp, link=link, gutachtentLink=Option(gutachtenLink))
  }
}