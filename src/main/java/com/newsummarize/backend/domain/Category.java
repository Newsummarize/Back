package com.newsummarize.backend.domain;

public enum Category {
    정치, 경제, 사회, 생활_문화("생활/문화"), 세계, IT_과학("IT/과학");

    private final String label;
    Category() { this.label = name(); }
    Category(String label) { this.label = label; }
    public String getLabel() { return label; }
}
