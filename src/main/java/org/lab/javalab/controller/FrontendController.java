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

    @GetMapping("/articles")
    public String articlesList(Model model) {
        model.addAttribute("articles", articleRepository.findAll());
        return "articles";
    }

    @PostMapping("/ui/articles/add")
    public String addArticle(@RequestParam String title, @RequestParam String content) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        articleRepository.save(article);
        return "redirect:/articles";
    }

    @GetMapping("/articles/{id}")
    public String viewArticle(@PathVariable Long id, Model model) {
        Article article = articleRepository.findById(id).orElseThrow();
        model.addAttribute("article", article);

        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize(article.getContent());
        List<ParsedWordDto> parsedWords = new ArrayList<>();

        for (Token token : tokens) {
            String pos = token.getPartOfSpeechLevel1();

            String baseForm = token.getBaseForm() != null && !token.getBaseForm().equals("*")
                    ? token.getBaseForm()
                    : token.getSurface();

            parsedWords.add(new ParsedWordDto(token.getSurface(), baseForm, pos));
        }
        model.addAttribute("parsedWords", parsedWords);

        return "article-view";
    }

    @PostMapping("/ui/articles/{id}/words/add")
    public String addWordToArticle(@PathVariable Long id, @RequestParam String word) {
        parserService.fetchAndLinkWordToArticle(word, id);
        return "redirect:/articles/" + id;
    }
    @GetMapping("/explore")
    public String exploreNews(Model model) {
        return "explore";
    }

    @GetMapping("/explore/{sourceId}")
    public String viewScrapedNews(@PathVariable String sourceId, Model model) {
        List<ScrapedArticleDto> newsList = newsScraperService.scrapeNews(sourceId);
        model.addAttribute("newsList", newsList);
        model.addAttribute("currentSource", sourceId);
        return "explore";
    }

    @PostMapping("/ui/articles/import")
    public String importArticleToDb(@RequestParam String title, @RequestParam String sourceUrl) {

        String fullText = newsScraperService.scrapeFullArticleText(sourceUrl);

        Article article = new Article();
        article.setTitle(title);
        article.setContent(fullText);
        articleRepository.save(article);

        return "redirect:/articles";
    }
    @PostMapping("/api/articles/{id}/words/add-ajax")
    @ResponseBody
    public ResponseEntity<Vocabulary> addWordAjax(@PathVariable Long id, @RequestParam String word) {
        Vocabulary vocab = parserService.fetchAndLinkWordToArticle(word, id);
        return ResponseEntity.ok(vocab);
    }
}