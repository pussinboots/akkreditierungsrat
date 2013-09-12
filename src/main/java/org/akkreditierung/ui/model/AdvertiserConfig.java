package org.akkreditierung.ui.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author frank
 * 
 */
@Entity
@Table(name = "studiengaenge")
public class AdvertiserConfig implements Serializable {

	private static final long serialVersionUID = -1587664618577852245L;
	@Id
	private int id;
	private String fach;
    private String abschluss;
    private String hochschule;
    private String bezugstyp;

    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFach() {
		return fach;
	}

	public void setFach(String fach) {
		this.fach = fach;
	}

    public String getAbschluss() {
        return abschluss;
    }

    public void setAbschluss(String abschluss) {
        this.abschluss = abschluss;
    }

    public String getHochschule() {
        return hochschule;
    }

    public void setHochschule(String hochschule) {
        this.hochschule = hochschule;
    }

    public String getBezugstyp() {
        return bezugstyp;
    }

    public void setBezugstyp(String bezugstyp) {
        this.bezugstyp = bezugstyp;
    }
}
