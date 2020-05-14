package de.unidue.inf.is.domain;


public class Comment {
    final String name;
    final String text;

    public Comment(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}
