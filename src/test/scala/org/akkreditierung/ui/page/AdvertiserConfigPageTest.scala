package org.akkreditierung.ui.page

import org.specs2.mutable._
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.test.SlickDbBefore
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
import org.akkreditierung.ui.WicketApplication
import org.akkreditierung.model.slick._
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import org.akkreditierung.DateUtil
import org.akkreditierung.model.slick.Studiengang
import org.akkreditierung.model.slick.StudiengangAttribute
import scala.Some
import org.specs2.specification.Outside
import scala.slick.driver.H2Driver

class AdvertiserConfigPageTest extends Specification with SlickDbBefore {

  sys.props.+=("Database" -> "h2")

  implicit val wicketTester = new Outside[WicketTester] {
    def outside: WicketTester = {
      val wt = new WicketTester(new WicketApplication())
      wt.startPage(classOf[AdvertiserConfigPage])
      wt
    }
  }

  override def initTestData(db: Database) {
    db withDynSession {
      val dao = new DAL(H2Driver)
      import dao._
      import dao.profile.simple._
      def studiengang1 = new Studiengang(Some(1), jobId = Some(1), fach = "Angewandte Informatik", abschluss = "Master", hochschule = "Potsdam Universität", bezugstyp = "bezug", gutachtenLink = Some("gutachtenlink"), link = Some("link"), updateDate = DateUtil.nowDateTimeOpt(), modifiedDate = DateUtil.nowDateTimeOpt(), sourceId = 1, checkSum = "1")
      def studiengang2 = new Studiengang(Some(2), jobId = Some(2), fach = "Soziologie", abschluss = "Bachelor", hochschule = "Mainz Universität", bezugstyp = "bezug", link = Some("link2"), updateDate = DateUtil.nowDateTimeOpt(), modifiedDate = DateUtil.nowDateTimeOpt(), sourceId = 1, checkSum = "2")
      studiengangs.insertAll(studiengang1, studiengang2)
      def studiengangAttribute1 = new StudiengangAttribute(1, "k1", "v1")
      def studiengangAttribute2 = new StudiengangAttribute(2, "von", "Acquinn")
      def studiengangAttribute3 = new StudiengangAttribute(2, "k1", "v1")
      studiengangAttributes.insertAll(studiengangAttribute1, studiengangAttribute2, studiengangAttribute3)
    }
  }

  sequential
  "AdvertiserConfigPage" should {
    "with no filter" in {
      wt: WicketTester =>
        wt.assertComponent("datatable", classOf[DataTable[Studiengang, String]])
        def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[Studiengang, String]]
        table.getRowCount() must beEqualTo(2)
    }

    "with hochschul filter" in {wt: WicketTester =>
        wt.newFormTester("filterForm").setValue("hochschule", "Potsdam")
        wt.executeAjaxEvent("filterForm:hochschule", "onchange");
        wt.assertComponent("datatable", classOf[DataTable[Studiengang, String]])
        def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[Studiengang, String]]
        table.getRowCount() must beEqualTo(1)
        val data = table.getDataProvider().iterator(table.getItemsPerPage() * table.getCurrentPage(), table.getItemsPerPage())
        data.next.fach must beEqualTo("Angewandte Informatik")
    }

    "with jobid filter" in {wt: WicketTester =>
        wt.newFormTester("filterForm").setValue("jobId", "1")
        wt.executeAjaxEvent("filterForm:jobId", "onchange");
        wt.assertComponent("datatable", classOf[DataTable[Studiengang, String]])
        def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[Studiengang, String]]
        table.getRowCount() must beEqualTo(1)
        val data = table.getDataProvider().iterator(table.getItemsPerPage() * table.getCurrentPage(), table.getItemsPerPage())
        data.next.fach must beEqualTo("Angewandte Informatik")
    }

    "with agentur filter" in {wt: WicketTester =>
      wt.newFormTester("filterForm").setValue("agentur", "Acquinn")
      wt.executeAjaxEvent("filterForm:agentur", "onchange");
      wt.assertComponent("datatable", classOf[DataTable[Studiengang, String]])
      def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[Studiengang, String]]
      table.getRowCount() must beEqualTo(1)
      val data = table.getDataProvider().iterator(table.getItemsPerPage() * table.getCurrentPage(), table.getItemsPerPage())
      data.next.fach must beEqualTo("Soziologie")
    }
  }
//  "AdvertiserConfigPage LinkPanel" should {
//    "click open here link for the firts studiengang" in {wt: WicketTester =>
//        wt.assertComponent("datatable", classOf[DataTable[Studiengang, String]])
//        wt.clickLink("datatable:body:rows:1:cells:8:cell:select")
//        wt.assertComponent("selected:displayPanel", classOf[DataView[StudiengangAttribute]])
//        def table = wt.getComponentFromLastRenderedPage("selected:displayPanel").asInstanceOf[DataView[StudiengangAttribute]]
//        table.getRowCount() must beEqualTo(1)
//        import scala.collection.JavaConversions._
//        table.getItems().find(it => it.getModelObject.key == "k1").get.getModelObject.id must beEqualTo(1)
//        table.getItems().find(it => it.getModelObject.key == "k1").get.getModelObject.key must beEqualTo("k1")
//        table.getItems().find(it => it.getModelObject.key == "k1").get.getModelObject.value must beEqualTo("v1")
//    }
//  }
}
