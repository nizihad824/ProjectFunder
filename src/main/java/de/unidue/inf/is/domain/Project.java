package de.unidue.inf.is.domain;

import java.math.BigDecimal;

public class Project {
    private int id;
    private String title;
    private String name;
    private String status;
    private String icon;
    private java.math.BigDecimal sum;
    private String userEmail;

    public Project() {
    }


    public Project(int id, String title, String name, String status, String icon, BigDecimal sum, String userEmail) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.status = status;
        this.icon = icon;
        this.sum = sum;
        this.userEmail = userEmail;
    }

    public Project(int id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id + '\'' +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", icon='" + icon + '\'' +
                ", sum=" + sum +
                ", userEmail='" + userEmail + '\'' +
                '}';
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
