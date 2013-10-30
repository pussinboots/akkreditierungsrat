package org.akkreditierung.ui.page

import org.specs2.mutable._
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.model.{Studiengang, StudiengangAttribute, DB}
import org.akkreditierung.test.EBean
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.markup.repeater.data.DataView
import org.akkreditierung.ui.model.StudiengaengeAttribute

class StudiengangDetailPageTest extends Specification with Before {
  def before {
    val jdbc = DB.getHSqlConnection()
    EBean.initDataSource("org.hsqldb.jdbc.JDBCDriver", jdbc)
    DB.createTables()

    def studiengangAttribute1 = new StudiengangAttribute(1, "k1", "v1")
    def studiengangAttribute2 = new StudiengangAttribute(1, "k2", "v2")
    StudiengangAttribute.Inserts(Seq(studiengangAttribute1, studiengangAttribute2))
  }

  "StudiengangDetailPage" should {
    "construct without error" in {
      val wt = new WicketTester
      val p = wt.startPage(classOf[StudiengangDetailPage], new PageParameters().add(StudiengangDetailPage.PAGE_PARAMETER_ID, "1"))
      wt.assertComponent("displayPanel", classOf[DataView[StudiengaengeAttribute]])
      def table = wt.getComponentFromLastRenderedPage("displayPanel").asInstanceOf[DataView[StudiengaengeAttribute]]
      table.getRowCount() must beEqualTo(2)
      import scala.collection.JavaConversions._
      table.getItems().find(it => it.getModelObject.k == "k1").get.getModelObject.id must beEqualTo(1)
      table.getItems().find(it => it.getModelObject.k == "k1").get.getModelObject.k must beEqualTo("k1")
      table.getItems().find(it => it.getModelObject.k == "k1").get.getModelObject.v must beEqualTo("v1")

      table.getItems().find(it => it.getModelObject.k == "k2").get.getModelObject.id must beEqualTo(1)
      table.getItems().find(it => it.getModelObject.k == "k2").get.getModelObject.k must beEqualTo("k2")
      table.getItems().find(it => it.getModelObject.k == "k2").get.getModelObject.v must beEqualTo("v2")
    }
  }

  step {
    DB.shutdownHSqlConnection("jdbc:hsqldb:mem:hsqldb:test")
  }
}
