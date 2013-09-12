package org.akkreditierung.ui;

import org.akkreditierung.ui.page.AdvertiserConfigPage;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

public class WicketApplication extends WebApplication {

	@Override
	public Class<? extends WebPage> getHomePage() {
		return AdvertiserConfigPage.class;
	}

	@Override
	public void init() {
		super.init();

		mountPage("configs", AdvertiserConfigPage.class);
//		mountPage("config", AdvertiserConfigDetailPage.class);
//		mountPage("newconfig", AdvertiserConfigNewPage.class);
//		mountPage("crawler", CrawlerStartPage.class);
		// add your configuration here
	}
}
