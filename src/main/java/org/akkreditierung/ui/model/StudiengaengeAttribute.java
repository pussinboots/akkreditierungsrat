package org.akkreditierung.ui.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author frank
 * 
 */
@Entity
@Table(name = "studiengaenge_attribute")
public class StudiengaengeAttribute implements Serializable {

	private static final long serialVersionUID = -1587664618577852245L;
	private int id;
	private String k;
    private String v;

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getK() {
		return k;
	}

	public void setK(String k) {
		this.k = k;
	}

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    @Override
    public String toString() {
        return "StudiengaengeAttribute{" +
                "id=" + id +
                ", k='" + k + '\'' +
                ", v='" + v + '\'' +
                '}';
    }
}
