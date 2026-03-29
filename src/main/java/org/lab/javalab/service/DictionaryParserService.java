package org.lab.javalab.service;

import org.lab.javalab.model.Article;
import org.lab.javalab.model.Vocabulary;
import org.lab.javalab.repository.ArticleRepository;
import org.lab.javalab.repository.VocabularyRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class DictionaryParserService {

    private final VocabularyRepository vocabularyRepository;
    private final ArticleRepository articleRepository;

    public DictionaryParserService(VocabularyRepository vocabularyRepository, ArticleRepository articleRepository) {
        this.vocabularyRepository = vocabularyRepository;
        this.articleRepository = articleRepository;
    }

    public Vocabulary fetchAndSaveWordInfo(String word) {
        return vocabularyRepository.findByWord(word).orElseGet(() -> {
            try {
                String url = "https://jisho.org/search/" + word;
                Document doc = Jsoup.connect(url).get();

                Element readingElement = doc.selectFirst(".exact_block .furigana");
                Element meaningElement = doc.selectFirst(".exact_block .meaning-meaning");

                String reading = (readingElement != null) ? readingElement.text() : "Невідомо";
                String meaning = (meaningElement != null) ? meaningElement.text() : "Не знайдено";

                Vocabulary newVocab = new Vocabulary(word, reading, meaning);
                return vocabularyRepository.save(newVocab);

            } catch (IOException e) {
                throw new RuntimeException("Помилка парсингу слова: " + word, e);
            }
        });
    }
    @Transactional
    public Vocabulary fetchAndLinkWordToArticle(String word, Long articleId) {
        Vocabulary vocab = fetchAndSaveWordInfo(word);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Статтю не знайдено"));

        article.addVocabulary(vocab);
        articleRepository.save(article);

        return vocab;
    }
}