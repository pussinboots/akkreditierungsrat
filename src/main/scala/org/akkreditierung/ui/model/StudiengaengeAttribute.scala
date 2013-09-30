package org.akkreditierung.ui.model

import javax.persistence.{Id, Transient, Entity, Table}
import java.io.Serializable
import scala.beans.BeanProperty

@Entity
@Table(name = "studiengaenge_attribute")
@SerialVersionUID(-1587664618577852245L)
class StudiengaengeAttribute extends Serializable {

  override def toString: String = {
    "StudiengaengeAttribute{" + "id=" + id + ", k='" + k + '\'' + ", v='" + v + '\'' + '}'
  }

  @Id @BeanProperty var id: Int = 0
  @BeanProperty var k: String = null
  @BeanProperty var v: String = null
}