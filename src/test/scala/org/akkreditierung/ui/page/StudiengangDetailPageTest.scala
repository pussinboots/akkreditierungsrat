package org.akkreditierung.ui.page

import org.specs2.mutable._
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.test.{SlickDbBefore, HSQLDbBefore}
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.markup.repeater.data.DataView
import org.akkreditierung.ui.WicketApplication
import org.akkreditierung.model.slick.StudiengangAttribute
import org.akkreditierung.model.DB
import scala.slick.session.Database
import Database.threadLocalSession

class StudiengangDetailPageTest extends Specification with SlickDbBefore {

  sys.props.+=("Database" -> "h2")

  override def initTestData(db: Database) {
    def studiengangAttribute1 = new StudiengangAttribute(1, "k1", "v1")
    def studiengangAttribute2 = new StudiengangAttribute(1, "k2", "v2")
    import DB.dal.profile.simple._
    db withSession {
      DB.dal.drop
      DB.dal.create
      DB.dal.StudiengangAttributes.insertAll(studiengangAttribute1, studiengangAttribute2)
    }
  }

  "StudiengangDetailPage" should {
    "construct without error" in {
      val wt = new WicketTester(new WicketApplication())
      val p = wt.startPage(classOf[StudiengangDetailPage], new PageParameters().add(StudiengangDetailPage.PAGE_PARAMETER_ID, "1"))
      wt.assertComponent("displayPanel", classOf[DataView[StudiengangAttribute]])
      def table = wt.getComponentFromLastRenderedPage("displayPanel").asInstanceOf[DataView[StudiengangAttribute]]
      table.getRowCount() must beEqualTo(2)
      import scala.collection.JavaConversions._
      table.getItems().find(it => it.getModelObject.key == "k1").get.getModelObject.id must beEqualTo(1)
      table.getItems().find(it => it.getModelObject.key == "k1").get.getModelObject.key must beEqualTo("k1")
      table.getItems().find(it => it.getModelObject.key == "k1").get.getModelObject.value must beEqualTo("v1")

      table.getItems().find(it => it.getModelObject.key == "k2").get.getModelObject.id must beEqualTo(1)
      table.getItems().find(it => it.getModelObject.key == "k2").get.getModelObject.key must beEqualTo("k2")
      table.getItems().find(it => it.getModelObject.key == "k2").get.getModelObject.value must beEqualTo("v2")
    }
  }
}
