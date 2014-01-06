package org.akkreditierung.ui

import org.akkreditierung.ui.page.{MySignInPage, StudiengangEditPage, StudiengangDetailPage, AdvertiserConfigPage}
import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.authroles.authentication.{AuthenticatedWebApplication, AbstractAuthenticatedWebSession}
import org.akkreditierung.ui.auth.AuthSession
import org.apache.wicket.ConverterLocator
import org.apache.wicket.util.convert.converter.AbstractConverter
import java.util.Locale
import scala.None

class OptionIntConverter extends AbstractConverter[Option[_]] {
  override def convertToObject(value: String, locale: Locale ) : Option[_] = if (value == null)  None else Some(value)
  override def convertToString(value: Option[_], locale: Locale) = value.getOrElse("").toString
  def getTargetType: Class[Option[_]] = classOf[Option[_]]
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
    locator.set(classOf[Some[Int]], new OptionIntConverter)
    locator
  }

  override def getWebSessionClass: Class[_ <:AbstractAuthenticatedWebSession] = classOf[AuthSession]

  override def getSignInPageClass: Class[_ <:WebPage] = classOf[MySignInPage]
}