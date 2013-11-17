package org.wicket.scala

import org.specs2.mutable.Specification
import org.apache.wicket.markup.html.form.TextField
import org.wicket.scala.Fields.AjaxOnChangeBehavoir
import org.apache.wicket.util.tester.WicketTester
import org.akkreditierung.ui.WicketApplication

class FieldsSpec extends Specification {

  "Fields" should {
    "add ajax chaange listener" in {
        new WicketTester(new WicketApplication())
      val textField = Fields.addOnChange(new TextField[String]("testId"))
      textField.getBehaviors.size() must beEqualTo(1)
      textField.getBehaviors.get(0).getClass must beEqualTo(classOf[AjaxOnChangeBehavoir])
    }
  }
}
