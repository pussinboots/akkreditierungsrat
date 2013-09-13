package org.akkreditierung.ui

import org.akkreditierung.ui.page.AdvertiserConfigPage
import org.apache.wicket.markup.html.WebPage
import org.apache.wicket.protocol.http.WebApplication

class WicketApplication extends WebApplication {
  def getHomePage: Class[_ <: WebPage] = classOf[AdvertiserConfigPage]

  override def init {
    mountPage("configs", classOf[AdvertiserConfigPage])
  }
}