package org.lab.javalab.controller;

import org.lab.javalab.model.Article;
import org.lab.javalab.repository.ArticleRepository;
import org.lab.javalab.repository.VocabularyRepository;
import org.lab.javalab.service.DictionaryParserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.lab.javalab.dto.ScrapedArticleDto;
import org.lab.javalab.service.NewsScraperService;
import java.util.List;
import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import org.lab.javalab.dto.ParsedWordDto;
import org.lab.javalab.model.Vocabulary;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;

@Controller
public class FrontendController {

    private final VocabularyRepository vocabularyRepository;
    private final ArticleRepository articleRepository; // Додали сюди
    private final DictionaryParserService parserService;
    private final NewsScraperService newsScraperService;

    public FrontendController(VocabularyRepository vocabularyRepository,
                              ArticleRepository articleRepository,
                              DictionaryParserService parserService,
                              NewsScraperService newsScraperService) {
        this.vocabularyRepository = vocabularyRepository;
        this.articleRepository = articleRepository;
        this.parserService = parserService;
        this.newsScraperService = newsScraperService;
    }

    // --- РОБОТА ЗІ СЛОВНИКОМ (Головна сторінка) ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("words", vocabularyRepository.findAll());
        return "index";
    }

    @PostMapping("/ui/vocabulary/add")
    public String addWordFromUI(@RequestParam String word) {
        parserService.fetchAndSaveWordInfo(word);
        return "redirect:/";
    }

    @PostMapping("/ui/vocabulary/delete")
    public String deleteWordFromUI(@RequestParam Long id) {
        vocabularyRepository.deleteById(id);
        return "redirect:/";
    }

    // --- НОВЕ: РОБОТА ЗІ СТАТТЯМИ ---

    // 1. Сторінка списку статей
    @GetMapping("/articles")
    public String articlesList(Model model) {
        model.addAttribute("articles", articleRepository.findAll());
        return "articles";
    }

    // 2. Додати нову статтю
    @PostMapping("/ui/articles/add")
    public String addArticle(@RequestParam String title, @RequestParam String content) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        articleRepository.save(article);
        return "redirect:/articles";
    }

    // 3. Відкрити статтю для читання
    @GetMapping("/articles/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        Article article = articleRepository.findById(id).orElseThrow();
        model.addAttribute("article", article);

        // Ініціалізуємо японський аналізатор
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize(article.getContent());
        List<ParsedWordDto> parsedWords = new ArrayList<>();

        for (Token token : tokens) {
            String pos = token.getPartOfSpeechLevel1(); // Отримуємо частину мови (японською)

            // Якщо слово змінене (напр. минулий час), беремо його словникову форму, інакше - як є
            String baseForm = token.getBaseForm() != null && !token.getBaseForm().equals("*")
                    ? token.getBaseForm()
                    : token.getSurface();

            parsedWords.add(new ParsedWordDto(token.getSurface(), baseForm, pos));
        }

        // Передаємо список розібраних слів на сторінку
        model.addAttribute("parsedWords", parsedWords);

        return "article-view";
    }

    // 4. Додати слово безпосередньо до статті
    @PostMapping("/ui/articles/{id}/words/add")
    public String addWordToArticle(@PathVariable Long id, @RequestParam String word) {
        parserService.fetchAndLinkWordToArticle(word, id);
        return "redirect:/articles/" + id; // Повертаємось на сторінку читання
    }
    @GetMapping("/explore")
    public String exploreNews(Model model) {
        return "explore";
    }

    // 2. Парсинг конкретного джерела при натисканні на посилання
    @GetMapping("/explore/{sourceId}")
    public String viewScrapedNews(@PathVariable String sourceId, Model model) {
        List<ScrapedArticleDto> newsList = newsScraperService.scrapeNews(sourceId);
        model.addAttribute("newsList", newsList);
        model.addAttribute("currentSource", sourceId);
        return "explore";
    }

    // 3. Збереження статті в базу даних при натисканні "Імпортувати"
    @PostMapping("/ui/articles/import")
    public String importArticleToDb(@RequestParam String title, @RequestParam String sourceUrl) {

        // 1. Йдемо за посиланням і парсимо ВЕСЬ текст статті
        String fullText = newsScraperService.scrapeFullArticleText(sourceUrl);

        // 2. Створюємо статтю і зберігаємо в БД
        Article article = new Article();
        article.setTitle(title);
        article.setContent(fullText); // Зберігаємо саме величезний спарсений текст
        articleRepository.save(article);

        return "redirect:/articles";
    }
    @PostMapping("/api/articles/{id}/words/add-ajax")
    @ResponseBody
    public ResponseEntity<Vocabulary> addWordAjax(@PathVariable Long id, @RequestParam String word) {
        // Викликаємо наш сервіс парсингу, який знаходить переклад і прив'язує до статті
        Vocabulary vocab = parserService.fetchAndLinkWordToArticle(word, id);
        return ResponseEntity.ok(vocab); // Повертаємо знайдене слово у форматі JSON
    }
}