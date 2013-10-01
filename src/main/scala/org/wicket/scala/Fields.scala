package org.wicket.scala

import org.apache.wicket.markup.html.form.TextField
import org.apache.wicket.model.Model
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.MarkupContainer

object Fields {
  def addOnChange(textField: TextField[String]) = textField.add(new AjaxOnChangeBehavoir)

  def createAjaxTextFilter(componentId: String, componentToAdd: MarkupContainer, block: TextField[String] => Unit = addOnChange): TextField[String] = {
    createTextFilter(componentId, componentToAdd, textField => textField.add(new AjaxOnChangeBehavoir))
  }

  def createTextFilter(componentId: String, componentToAdd: MarkupContainer, block: TextField[String] => Unit): TextField[String] = {
    val textFilter: TextField[String] = new TextField[String](componentId, new Model(""))
    block(textFilter)
    componentToAdd.add(textFilter)
    textFilter
  }

  class AjaxOnChangeBehavoir extends AjaxFormComponentUpdatingBehavior("onchange") {
    protected def onUpdate(target: AjaxRequestTarget) {
      target.add(target.getPage)
    }
  }
}
