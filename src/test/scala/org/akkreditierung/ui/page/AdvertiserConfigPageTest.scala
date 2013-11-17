package org.akkreditierung.ui.page

import org.specs2.mutable._
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.model.{StudiengangAttribute, Studiengang}
import org.akkreditierung.test.HSQLDbBefore
import org.akkreditierung.ui.model.{StudiengaengeAttribute, AdvertiserConfig}
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
import org.akkreditierung.ui.WicketApplication
import java.util.Date
import org.apache.wicket.markup.html.panel.Panel
import org.apache.wicket.markup.repeater.data.DataView

class AdvertiserConfigPageTest extends Specification with HSQLDbBefore {

  override def initTestData() {
    def studiengang1 = new Studiengang(jobId=Some(1), fach = "Angewandte Informatik", abschluss = "Master", hochschule = "Potsdam Universität", bezugstyp = "bezug", gutachtentLink = Some("gutachtenlink") , link = Some("link"), modifiedDate=Some(new Date()), sourceId= 1)
    def studiengang2 = new Studiengang(jobId=Some(2), fach = "Soziologie", abschluss = "Bachelor", hochschule = "Mainz Universität", bezugstyp = "bezug", link = Some("link2"), modifiedDate=Some(new Date()), sourceId= 1)
    val informatikStudienGang = Studiengang.Inserts(Seq(studiengang1, studiengang2)).find((st=>st.fach=="Angewandte Informatik")).get
    def studiengangAttribute1 = new StudiengangAttribute(informatikStudienGang.id.get, "k1", "v1")
    StudiengangAttribute.Inserts(Seq(studiengangAttribute1))
  }

  sequential
  "AdvertiserConfigPage" should {
    "with no filter" in {
      val wt = new WicketTester(new WicketApplication())
      val p = wt.startPage(classOf[AdvertiserConfigPage])
      wt.assertComponent("datatable", classOf[DataTable[AdvertiserConfig, String]])
      def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[AdvertiserConfig, String]]
      table.getRowCount() must beEqualTo(2)
    }

    "with hochschul filter" in {
      val wt = new WicketTester(new WicketApplication())
      val p = wt.startPage(classOf[AdvertiserConfigPage])
      wt.newFormTester("filterForm").setValue("hochschule", "Potsdam")
      wt.executeAjaxEvent("filterForm:hochschule", "onchange");
      wt.assertComponent("datatable", classOf[DataTable[AdvertiserConfig, String]])
      def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[AdvertiserConfig, String]]
      table.getRowCount() must beEqualTo(1)
      val data = table.getDataProvider().iterator(table.getItemsPerPage() * table.getCurrentPage(), table.getItemsPerPage())
      data.next.getFach() must beEqualTo("Angewandte Informatik")
    }

    "with jobid filter" in {
      val wt = new WicketTester(new WicketApplication())
      val p = wt.startPage(classOf[AdvertiserConfigPage])
      wt.newFormTester("filterForm").setValue("jobId", "1")
      wt.executeAjaxEvent("filterForm:jobId", "onchange");
      wt.assertComponent("datatable", classOf[DataTable[AdvertiserConfig, String]])
      def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[AdvertiserConfig, String]]
      table.getRowCount() must beEqualTo(1)
      val data = table.getDataProvider().iterator(table.getItemsPerPage() * table.getCurrentPage(), table.getItemsPerPage())
      data.next.getFach() must beEqualTo("Angewandte Informatik")
    }
  }
  "AdvertiserConfigPage LinkPanel" should {
    "click open here link for the firts studiengang" in {
      val wt = new WicketTester(new WicketApplication())
      val p = wt.startPage(classOf[AdvertiserConfigPage])
      wt.assertComponent("datatable", classOf[DataTable[AdvertiserConfig, String]])
      wt.clickLink("datatable:body:rows:1:cells:8:cell:select")
      wt.assertComponent("selected:displayPanel", classOf[DataView[StudiengaengeAttribute]])
      def table = wt.getComponentFromLastRenderedPage("selected:displayPanel").asInstanceOf[DataView[StudiengaengeAttribute]]
      table.getRowCount() must beEqualTo(1)
      import scala.collection.JavaConversions._
      table.getItems().find(it => it.getModelObject.k == "k1").get.getModelObject.id must beEqualTo(0)
      table.getItems().find(it => it.getModelObject.k == "k1").get.getModelObject.k must beEqualTo("k1")
      table.getItems().find(it => it.getModelObject.k == "k1").get.getModelObject.v must beEqualTo("v1")
    }
  }
}
