package org.akkreditierung.ui.model;

import com.avaje.ebean.Query;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.markup.html.form.TextField;

public class AdvertiserConfigModelProvider extends
		GenericProvider<AdvertiserConfig> {

	private static final AdvertiserConfigBean ADVERTISER_CONFIG_BEAN = new AdvertiserConfigBean();

	private static final long serialVersionUID = -6117562733583734933L;
    private FilterContainer filterContainer;

    public AdvertiserConfigModelProvider(FilterContainer filterContainer) {
		super(ADVERTISER_CONFIG_BEAN);
        this.filterContainer = filterContainer;
        setSort("id", SortOrder.ASCENDING);
	}

    @Override
    public Query<AdvertiserConfig> filter(Query<AdvertiserConfig> query) {
        filterContainer.apply(query);
        //query.fetch("map");
        return super.filter(query);
    }
}
