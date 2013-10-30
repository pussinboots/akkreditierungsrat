package org.akkreditierung.ui.page

import org.specs2.mutable._
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.model.{Studiengang, DB}
import org.akkreditierung.test.EBean
import org.akkreditierung.ui.model.AdvertiserConfig
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
import org.specs2.specification.BeforeAfterExample

class AdvertiserConfigPageTest extends Specification with Before {
  def before {
    val jdbcUrl = DB.getHSqlConnection()

    EBean.initDataSource("org.hsqldb.jdbc.JDBCDriver", jdbcUrl)
    DB.createTables()

    def studiengang1 = new Studiengang(fach = "Angewandte Informatik", abschluss = "Master", hochschule = "Potsdam Universität", bezugstyp = "bezug", link = "link")
    def studiengang2 = new Studiengang(fach = "Soziologie", abschluss = "Bachelor", hochschule = "Mainz Universität", bezugstyp = "bezug", link = "link2")
    Studiengang.Inserts(Seq(studiengang1, studiengang2))
  }

  sequential
  "AdvertiserConfigPage" should {
    "with no filter" in {

      val wt = new WicketTester
      val p = wt.startPage(classOf[AdvertiserConfigPage])
      wt.assertComponent("datatable", classOf[DataTable[AdvertiserConfig, String]])
      def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[AdvertiserConfig, String]]
      table.getRowCount() must beEqualTo(2)
    }

    "with hochschul filter" in {

      val wt = new WicketTester
      val p = wt.startPage(classOf[AdvertiserConfigPage])
      wt.newFormTester("filterForm").setValue("hochschule", "Potsdam")
      wt.executeAjaxEvent("filterForm:hochschule", "onchange");
      wt.assertComponent("datatable", classOf[DataTable[AdvertiserConfig, String]])
      def table = wt.getComponentFromLastRenderedPage("datatable").asInstanceOf[DataTable[AdvertiserConfig, String]]
      table.getRowCount() must beEqualTo(1)
      val data = table.getDataProvider().iterator(table.getItemsPerPage() * table.getCurrentPage(), table.getItemsPerPage())
      data.next.getFach() must beEqualTo("Angewandte Informatik")
    }
  }

  step {
    DB.shutdownHSqlConnection()
  }
}