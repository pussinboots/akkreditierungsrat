package org.akkreditierung.ui

import org.akkreditierung.ui.page.{MySignInPage, StudiengangEditPage, StudiengangDetailPage, AdvertiserConfigPage}
import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.protocol.http.WebApplication
import org.apache.wicket.authroles.authentication.{AuthenticatedWebApplication, AbstractAuthenticatedWebSession}
import org.akkreditierung.ui.auth.AuthSession

class WicketApplication extends AuthenticatedWebApplication {
  def getHomePage: Class[_ <: WebPage] = classOf[AdvertiserConfigPage]

  override def init {
    super.init()
    mountPage("configs", classOf[AdvertiserConfigPage])
    mountPage("detail", classOf[StudiengangDetailPage])
    mountPage("edit", classOf[StudiengangEditPage])
  }

  override def getWebSessionClass: Class[_ <:AbstractAuthenticatedWebSession] = {
    classOf[AuthSession]
  }

  override def getSignInPageClass: Class[_ <:WebPage] = {
    classOf[MySignInPage]
  }
}