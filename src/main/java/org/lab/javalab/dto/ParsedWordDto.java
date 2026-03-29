package org.lab.javalab.dto;

public class ParsedWordDto {
    private String surface;      // Як слово виглядає в тексті (напр. 食べた)
    private String baseForm;     // Словникова форма для пошуку (напр. 食べる)
    private String partOfSpeech; // Частина мови (Іменник, Дієслово тощо)

    public ParsedWordDto(String surface, String baseForm, String partOfSpeech) {
        this.surface = surface;
        this.baseForm = baseForm;
        this.partOfSpeech = partOfSpeech;
    }

    public String getSurface() { return surface; }
    public String getBaseForm() { return baseForm; }
    public String getPartOfSpeech() { return partOfSpeech; }
}