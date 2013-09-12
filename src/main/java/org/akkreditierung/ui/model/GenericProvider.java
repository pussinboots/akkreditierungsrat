package org.akkreditierung.ui.model;

import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Iterator;
import java.util.List;

public class GenericProvider<T> extends SortableDataProvider<T, String> {

	private final DefaultBean<T> bean;

	private static final long serialVersionUID = -6117562733583734933L;

	public GenericProvider(DefaultBean<T> defaultBean) {
		this.bean = defaultBean;
	}

	@Override
	public IModel<T> model(final T object) {
		return new LoadableDetachableModel<T>() {

			private static final long serialVersionUID = 5426947178517463427L;

			@Override
			protected T load() {
				return object;
			}
		};
	}

	private static int toInt(long value) {
		return Long.valueOf(value).intValue();
	}

	private OrderBy<T> orderBy(SortParam<String> param) {
		OrderBy<T> orderBy = new OrderBy<T>();
		if (param.isAscending()) {
			orderBy.asc(param.getProperty());
		} else {
			orderBy.desc(param.getProperty());
		}
		return orderBy;
	}
	
	public Query<T> filter(Query<T> query) {
		return query;
	}

	@Override
	public Iterator<T> iterator(long first, long count) {
        List<T> list = filter(bean.getQuery()).setOrderBy(orderBy(getSort())).setMaxRows(toInt(count)).setFirstRow(toInt(first))
                .findList();
		return list.iterator();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
	 */
	@Override
	public long size() {
		return filter(bean.getQuery()).findRowCount();
	}
}
