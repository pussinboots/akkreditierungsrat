package org.akkreditierung.ui

import org.akkreditierung.ui.page.{MySignInPage, StudiengangEditPage, StudiengangDetailPage, AdvertiserConfigPage}
import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.authroles.authentication.{AuthenticatedWebApplication, AbstractAuthenticatedWebSession}
import org.akkreditierung.ui.auth.AuthSession
import org.apache.wicket.ConverterLocator
import org.apache.wicket.util.convert.converter.AbstractConverter
import java.util.Locale

class OptionConverter extends AbstractConverter[Option[String]] {
  override def convertToObject(value: String, locale: Locale ) : Option[String] = {
    if (value == null)
      return None
    else
      return Some(value)
  }
  override def convertToString(value: Option[String], locale: Locale) = {
    value.getOrElse("")
  }

  def getTargetType: Class[Option[String]] = classOf[Option[String]]
}

class WicketApplication extends AuthenticatedWebApplication {
  def getHomePage: Class[_ <: WebPage] = classOf[AdvertiserConfigPage]

  override def init {
    super.init()
    mountPage("configs", classOf[AdvertiserConfigPage])
    mountPage("detail", classOf[StudiengangDetailPage])
    mountPage("edit", classOf[StudiengangEditPage])
    //todo activate automatic https redirect
    //setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new HttpsConfig(80, 443)));
    getDebugSettings().setAjaxDebugModeEnabled(false)
  }

  override def newConverterLocator() = {
    val locator = super.newConverterLocator().asInstanceOf[ConverterLocator]
    locator.set(classOf[Option[String]], new OptionConverter)
    locator
  }

  override def getWebSessionClass: Class[_ <:AbstractAuthenticatedWebSession] = classOf[AuthSession]

  override def getSignInPageClass: Class[_ <:WebPage] = classOf[MySignInPage]
}