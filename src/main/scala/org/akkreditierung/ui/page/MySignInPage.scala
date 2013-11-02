package org.akkreditierung.ui.page

import org.apache.wicket.authroles.authentication.panel.SignInPanel
import org.apache.wicket.request.mapper.parameter.PageParameters
import org.apache.wicket.markup.html.WebPage

class MySignInPage(parameters: PageParameters) extends WebPage(parameters) {
    add(new SignInPanel("signInPanel"))
}