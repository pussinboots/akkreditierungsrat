package org.akkreditierung.test

import org.specs2.mutable.Around
import org.specs2.execute.AsResult
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.ui.WicketApplication
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
import org.apache.wicket.Page

class WicketTest[P <: Page](pageClass: Class[P], component: String) extends Around {
  def around[T: AsResult](t: => T) = WicketTest.around(t, pageClass, component)
}

object WicketTest {
  def apply(pageClass: Class[_ <: Page], component: String) = new WicketTest(pageClass, component)

  def around[T: AsResult](t: => T, pageClass: Class[_ <: Page], component: String) = {
      val wicketTester = new WicketTester(new WicketApplication())
      val p: Page = wicketTester.startPage(pageClass)
      //wicketTester.assertComponent(component, classOf[DataTable[AdvertiserConfig, String]])
      try {
        implicit val page = p
        implicit val wt = wicketTester
        AsResult(t)
      } finally {
      }
  }
}
