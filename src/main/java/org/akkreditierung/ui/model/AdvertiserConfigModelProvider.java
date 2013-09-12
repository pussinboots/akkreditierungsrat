package org.akkreditierung.ui.model;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;

public class AdvertiserConfigModelProvider extends
		GenericProvider<AdvertiserConfig> {

	private static final AdvertiserConfigBean ADVERTISER_CONFIG_BEAN = new AdvertiserConfigBean();

	private static final long serialVersionUID = -6117562733583734933L;

	public AdvertiserConfigModelProvider() {
		super(ADVERTISER_CONFIG_BEAN);
		setSort("id", SortOrder.ASCENDING);
	}
}
