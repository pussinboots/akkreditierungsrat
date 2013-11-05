package org.akkreditierung.ui.model

import javax.persistence._
import java.util.Date
import scala.beans.BeanProperty

@Entity
@Table(name = "jobs") class Job {
  @Id @BeanProperty var id: Int = 0
  @BeanProperty @Column(name="createDate") var createDate: Date = null
  @BeanProperty @Column(name="newEntries") var newEntries: Int = 0
  @BeanProperty var status: String = null
}