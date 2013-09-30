package org.akkreditierung.ui.model

import javax.persistence._
import java.io.Serializable
import java.util.Map
import scala.beans.BeanProperty

@Entity
@Table(name = "studiengaenge") class AdvertiserConfig {
  @Id @BeanProperty var id: Int = 0
  @BeanProperty var fach: String = null
  @BeanProperty var abschluss: String = null
  @BeanProperty var hochschule: String = null
  @BeanProperty var bezugstyp: String = null
  @BeanProperty @Column(name = "`Gutachten Link`") var gutachtenLink: String = null
  @OneToMany(cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY)
  @JoinColumn(name = "id", referencedColumnName = "id", nullable = true)
  @MapKey(name = "k")
  @BeanProperty var map: Map[String, StudiengaengeAttribute] = null
}