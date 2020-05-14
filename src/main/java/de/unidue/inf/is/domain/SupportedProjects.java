package de.unidue.inf.is.domain;

import java.math.BigDecimal;

public class SupportedProjects {
    private int id;
    private String title;
    private String status;
    private String icon;

    public SupportedProjects(int id, String titel, String status, String icon, BigDecimal limit, BigDecimal beitrag) {
        this.id = id;
        this.title = titel;
        this.status = status;
        this.icon = icon;
        this.limit = limit;
        this.beitrag = beitrag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public BigDecimal getBeitrag() {
        return beitrag;
    }

    public void setBeitrag(BigDecimal beitrag) {
        this.beitrag = beitrag;
    }

    private java.math.BigDecimal limit;
    private java.math.BigDecimal beitrag;
}
