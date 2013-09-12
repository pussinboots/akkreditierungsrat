package org.akkreditierung.ui.model;


/**
 * @author frank
 * 
 */
public class AdvertiserConfigBean extends DefaultBean<AdvertiserConfig> {

	private static final long serialVersionUID = -650714867574087550L;

	public AdvertiserConfigBean() {
		super(DefaultBean.DB_ADVERTISER_CONFIG, AdvertiserConfig.class);
	}

}
