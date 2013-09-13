package org.akkreditierung.ui.model

import javax.persistence.{Transient, Entity, Table}
import java.io.Serializable
import scala.beans.BeanProperty

@Entity
@Table(name = "studiengaenge_attribute") class StudiengaengeAttribute extends Serializable {
  @Transient
  private final val serialVersionUID: Long = -1587664618577852245L

  override def toString: String = {
    return "StudiengaengeAttribute{" + "id=" + id + ", k='" + k + '\'' + ", v='" + v + '\'' + '}'
  }

  @BeanProperty var id: Int = 0
  @BeanProperty var k: String = null
  @BeanProperty var v: String = null
}