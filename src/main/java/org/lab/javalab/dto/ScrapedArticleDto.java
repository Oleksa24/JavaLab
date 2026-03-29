package org.lab.javalab.dto;

public class ScrapedArticleDto {
    private String title;
    private String content;
    private String sourceUrl;

    public ScrapedArticleDto(String title, String content, String sourceUrl) {
        this.title = title;
        this.content = content;
        this.sourceUrl = sourceUrl;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSourceUrl() { return sourceUrl; }
}