package org.akkreditierung.ui.page

import org.specs2.mutable._
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.model.Studiengang
import org.akkreditierung.test.HSQLDbBefore
import org.akkreditierung.ui.model.AdvertiserConfig
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
import org.akkreditierung.ui.WicketApplication

class AdvertiserConfigPageTest extends Specification with HSQLDbBefore {

  override def initTestData() {
    def studiengang1 = new Studiengang(jobId=Some(1), fach = "Angewandte Informatik", abschluss = "Master", hochschule = "Potsdam Universität", bezugstyp = "bezug", link = Some("link"), sourceId= 1)
    def studiengang2 = new Studiengang(jobId=Some(2), fach = "Soziologie", abschluss = "Bachelor", hochschule = "Mainz Universität", bezugstyp = "bezug", link = Some("link2"), sourceId= 1)
    Studiengang.Inserts(Seq(studiengang1, studiengang2))
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
}
