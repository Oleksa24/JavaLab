package org.lab.javalab.dto;

public class ParsedWordDto {
    private String surface;
    private String baseForm;
    private String partOfSpeech;

    public ParsedWordDto(String surface, String baseForm, String partOfSpeech) {
        this.surface = surface;
        this.baseForm = baseForm;
        this.partOfSpeech = partOfSpeech;
    }

    public String getSurface() { return surface; }
    public String getBaseForm() { return baseForm; }
    public String getPartOfSpeech() { return partOfSpeech; }
}