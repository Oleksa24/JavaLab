package org.lab.javalab.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;      // Ієрогліфи (наприклад: 先生)
    private String reading;   // Читання (наприклад: せんせい)
    private String meaning;   // Значення (наприклад: Вчитель)

    @ManyToMany(mappedBy = "vocabularies")
    private Set<Article> articles = new HashSet<>();

    // Конструктори, Getters та Setters
    public Vocabulary() {}

    public Vocabulary(String word, String reading, String meaning) {
        this.word = word;
        this.reading = reading;
        this.meaning = meaning;
    }

    public Long getId() { return id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getReading() { return reading; }
    public void setReading(String reading) { this.reading = reading; }
    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public Set<Article> getArticles() {
        return articles;
    }

    @PreRemove
    private void removeAssociations() {
        for (Article article : this.articles) {
            article.getVocabularies().remove(this);
        }
    }
}