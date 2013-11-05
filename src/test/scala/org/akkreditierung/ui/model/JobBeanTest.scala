package org.akkreditierung.ui.model

import org.specs2.mutable.Specification
import org.akkreditierung.test.HSQLDbBefore

class JobBeanTest extends Specification with HSQLDbBefore {

  override def initTestData() {
    org.akkreditierung.model.Job.Insert(new org.akkreditierung.model.Job(newEntries=11))
  }

  "JobBean" should {
    "return the latest job from database" in {
      val job = new JobBean().findLatest()
      org.akkreditierung.model.Job.Delete(new org.akkreditierung.model.Job(id=Option(job.id)))
      job.newEntries must beEqualTo(11)
      job.status must beEqualTo("started")
    }
  }
}
