package org.akkreditierung.ui.page;

import org.akkreditierung.ui.model.AdvertiserConfig;
import org.akkreditierung.ui.model.AdvertiserConfigModelProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

public class AdvertiserConfigPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public AdvertiserConfigPage(final PageParameters parameters) {
		super(parameters);

		DefaultDataTable table = new DefaultDataTable("datatable", getColumns(), new AdvertiserConfigModelProvider(), 25);
		add(table);
		//add(new BookmarkablePageLink("createlink", AdvertiserConfigNewPage.class));
	}

	private List<IColumn> getColumns() {
		List<IColumn> columns = new ArrayList<IColumn>();

		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Id"), "id", "id"));
		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Fach"), "fach", "fach"));
		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Abschluss"), "abschluss", "abschluss"));
		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Hochschule"), "hochschule", "hochschule"));
        columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Bezugstyp"), "bezugstyp", "bezugstyp"));
		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Aktion"), "id") {
			@Override
			public void populateItem(Item<ICellPopulator<AdvertiserConfig>> item, String componentId, IModel<AdvertiserConfig> rowModel) {
				item.add(new LinkPanel(componentId, rowModel.getObject()));
			}
		});
		return columns;
	}

	private class LinkPanel extends Panel {
		public LinkPanel(String id, AdvertiserConfig advertiserConfig) {
			super(id);
			PageParameters param = new PageParameters();
			param.add("advertiserConfigId", advertiserConfig.getId());
			BookmarkablePageLink link = new BookmarkablePageLink("link", AdvertiserConfigPage.class, param);
			link.add(new Label("label", "bearbeiten"));
			add(link);
		}
	}
}
