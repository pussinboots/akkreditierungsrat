package org.akkreditierung.ui.page;

import org.akkreditierung.ui.model.*;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StudiengangDetailPage extends WebPage {
    public static final String PAGE_PARAMETER_ID = "studiengangId";
	private static final long serialVersionUID = 1L;
    private AdvertiserConfig selected;

	public StudiengangDetailPage(final PageParameters parameters) {
		super(parameters);
        Map<String, StudiengaengeAttribute> studiengaengeAttributes = new StudiengaengeAttributeBean().findAll(parameters.get(PAGE_PARAMETER_ID).toInt(1));
        ListDataProvider<StudiengaengeAttribute> provider = new ListDataProvider<StudiengaengeAttribute>(new ArrayList(studiengaengeAttributes.values()));
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
