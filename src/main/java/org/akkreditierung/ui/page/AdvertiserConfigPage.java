package org.akkreditierung.ui.page;

import org.akkreditierung.ui.model.AdvertiserConfig;
import org.akkreditierung.ui.model.AdvertiserConfigModelProvider;
import org.akkreditierung.ui.model.FilterContainer;
import org.akkreditierung.ui.model.StudiengaengeAttribute;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.*;

public class AdvertiserConfigPage extends WebPage {
	private static final long serialVersionUID = 1L;
    private AdvertiserConfig selected;

	public AdvertiserConfigPage(final PageParameters parameters) {
		super(parameters);
        MarkupContainer detailPanel = new DetailPanel("selected", new PropertyModel<AdvertiserConfig>(this, "selected"));
        add(detailPanel);

        FilterContainer filterContainer = createFilterContainer();
        DefaultDataTable table = new DefaultDataTable("datatable", getColumns(detailPanel), new AdvertiserConfigModelProvider(filterContainer), 25);
		add(table);
	}

    private FilterContainer createFilterContainer() {
        TextField<String> hochSchule = createTextFilter("hochschule");
        TextField<String> fach = createTextFilter("fach");
        TextField<String> abschluss = createTextFilter("abschluss");
        TextField<String> agentur = createTextFilter("agentur");
        TextField<String> studienform = createTextFilter("studienform");
        return new FilterContainer(hochSchule, fach, abschluss, agentur, studienform);
    }

    private TextField<String> createTextFilter(String componentId) {
        TextField<String> textFilter = new TextField<String>(componentId, new Model(""));
        textFilter.add(new AjaxOnChangeBehavoir());
        add(textFilter);
        return textFilter;
    }

    private List<IColumn> getColumns(final MarkupContainer detailPanel) {
		final List<IColumn> columns = new ArrayList<IColumn>();

		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Id"), "id", "id"));
		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Fach"), "fach", "fach"));
		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Abschluss"), "abschluss", "abschluss"));
		columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Hochschule"), "hochschule", "hochschule"));
        columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Bezugstyp"), "bezugstyp", "bezugstyp"));
        columns.add(new PropertyColumn<AdvertiserConfig, String>(new Model<String>("Aktion"), "id") {
            @Override
            public void populateItem(Item<ICellPopulator<AdvertiserConfig>> item, String componentId, IModel<AdvertiserConfig> rowModel) {
                item.add(new ActionPanel(componentId, rowModel, detailPanel));
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

    private class AjaxOnChangeBehavoir extends AjaxFormComponentUpdatingBehavior {
        private AjaxOnChangeBehavoir() {
            super("onchange");
        }

        protected void onUpdate(AjaxRequestTarget target) {
        }
    }

    private class ActionPanel extends Panel
    {
        public ActionPanel(String id, IModel<AdvertiserConfig> model, final MarkupContainer detailPanel)
        {
            super(id, model);
            add(new AjaxLink("select")
            {
                @Override
                public void onClick(AjaxRequestTarget target)
                {
                    setSelected((AdvertiserConfig) getParent().getDefaultModelObject());
                    target.add(detailPanel);
                }
            });
        }
    }

    public AdvertiserConfig getSelected()
    {
        return selected;
    }

    public void setSelected(AdvertiserConfig selected)
    {
        addStateChange();
        this.selected = selected;
    }

    private class  DetailPanel extends Panel {
        public DetailPanel(String id, IModel<AdvertiserConfig> model)
        {
            super(id, model);
            setOutputMarkupId(true);
            final IModel<Map<String, StudiengaengeAttribute>> mapModel = new PropertyModel<Map<String, StudiengaengeAttribute>>(model, "map");
            ListDataProvider<StudiengaengeAttribute> provider = new ListDataProvider<StudiengaengeAttribute>() {
                @Override
                protected List getData() {
                    Map map = (mapModel.getObject() == null)? Collections.<String, StudiengaengeAttribute>emptyMap(): mapModel.getObject();
                    return new ArrayList(map.values());    //To change body of overridden methods use File | Settings | File Templates.
                }
            };
            DataView<StudiengaengeAttribute> dataView = new DataView<StudiengaengeAttribute>("displayPanel", provider) {
                @Override
                protected void populateItem(Item<StudiengaengeAttribute> item) {
                    StudiengaengeAttribute entry = item.getModelObject();
                    item.add(new Label("key_column", entry.getK()));
                    item.add(new Label("value_column", entry.getV()));
                }
            };
            add(dataView);
        }
    }
}
