package org.wicket.scala

import org.apache.wicket.markup.html.form.{HiddenField, TextField}
import org.apache.wicket.model.Model
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.MarkupContainer
import org.apache.wicket.markup.html.basic.Label
import org.akkreditierung.HTML4Escaper

object Fields {

  val pageParametersDefault = new PageParameters()
  def addOnChange(textField: TextField[String]) = {
    textField.add(new AjaxOnChangeBehavoir)
    textField
  }

  def createAjaxTextFilter(componentId: String, componentToAdd: MarkupContainer, pageParameters: PageParameters = pageParametersDefault, block: TextField[String] => TextField[String] = addOnChange): TextField[String] = {
    createTextFilter(componentId, componentToAdd, block, pageParameters.get(componentId).toString("") )
  }

  def createAjaxHiddenTextFilter(componentId: String, componentToAdd: MarkupContainer, block: TextField[String] => TextField[String] = addOnChange): TextField[String] = {
    createHiddenTextField(componentId, componentToAdd, block)
  }

  def createTextFilter(componentId: String, componentToAdd: MarkupContainer, block: TextField[String] => TextField[String], value: String = ""): TextField[String] = {
    val textFilter: TextField[String] = new TextField[String](componentId, new Model(value))
    componentToAdd.add(block(textFilter))
    textFilter
  }

  def createHiddenTextField(componentId: String, componentToAdd: MarkupContainer, block: TextField[String] => TextField[String]): TextField[String] = {
    val textFilter: TextField[String] = new HiddenField[String](componentId, new Model(""))
    componentToAdd.add(block(textFilter))
    textFilter
  }

  class AjaxOnChangeBehavoir extends AjaxFormComponentUpdatingBehavior("onchange") {
    protected def onUpdate(target: AjaxRequestTarget) {
      target.add(target.getPage)
    }
  }

  def labelWithSpecialEscaping(componentId: String, value: String): Label = {
    val label = new Label(componentId, HTML4Escaper.escapeHtml4(value))
    label.setEscapeModelStrings(false)
    label
  }
}
