package org.akkreditierung.ui.model;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Query;
import org.apache.wicket.markup.html.form.TextField;

public class FilterContainer {
    private TextField<String> hochSchule;
    private TextField<String> fach;
    private TextField<String> abschluss;

    public FilterContainer(TextField<String> hochSchule, TextField<String> fach, TextField<String> abschluss) {
        this.hochSchule = hochSchule;
        this.fach = fach;
        this.abschluss = abschluss;
    }

    public String getHochSchule() {
        return hochSchule.getValue();
    }

    public String getFach() {
        return fach.getValue();
    }

    public String getAbschluss() {
        return abschluss.getValue();
    }

    public <T> void  apply(Query<T> query) {
        ExpressionList<T> where = query.where();
        if (getHochSchule() != null && getHochSchule().length() > 0) {
            where.like("hochschule", getHochSchule());
        }
        if (getFach() != null && getFach().length() > 0) {
            where.like("fach", getFach());
        }
        if (getAbschluss() != null && getAbschluss().length() > 0) {
            where.like("abschluss", getAbschluss());
        }
    }
}
