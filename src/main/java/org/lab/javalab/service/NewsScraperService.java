package org.lab.javalab.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lab.javalab.dto.ScrapedArticleDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsScraperService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    public List<ScrapedArticleDto> scrapeNews(String sourceId) {
        List<ScrapedArticleDto> articles = new ArrayList<>();
        String url = "";

        switch (sourceId) {
            case "yahoo-top":
                url = "https://news.yahoo.co.jp/rss/topics/top-picks.xml";
                break;
            case "yahoo-science":
                url = "https://news.yahoo.co.jp/rss/topics/science.xml";
                break;
            case "nhk-news":
                url = "https://www.nhk.or.jp/rss/news/cat0.xml";
                break;
            case "itmedia":
                url = "https://rss.itmedia.co.jp/rss/2.0/itmedia_all.xml"; // IT-новини
                break;
            case "zenn":
                url = "https://zenn.dev/feed";
                break;
            default:
                return articles;
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(5000)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");

            for (int i = 0; i < Math.min(items.size(), 10); i++) {
                Element item = items.get(i);
                String title = item.select("title").text();
                String previewLink = item.select("link").text();
                String description = item.select("description").text();

                String realArticleLink = previewLink;

                if (previewLink.contains("news.yahoo.co.jp/pickup/")) {
                    try {
                        Document pickupDoc = Jsoup.connect(previewLink)
                                .userAgent(USER_AGENT)
                                .timeout(3000)
                                .get();

                        Element readMoreBtn = pickupDoc.selectFirst("a[href*=/articles/]");
                        if (readMoreBtn != null) {
                            realArticleLink = readMoreBtn.attr("abs:href");
                        }
                    } catch (IOException e) {
                        System.err.println("Помилка глибинного парсингу: " + e.getMessage());
                    }
                }

                articles.add(new ScrapedArticleDto(title, description, realArticleLink));
            }
        } catch (IOException e) {
            System.err.println("Головна помилка парсингу: " + e.getMessage());
        }

        return articles;
    }

    public String scrapeFullArticleText(String articleUrl) {
        try {
            Document doc = Jsoup.connect(articleUrl)
                    .userAgent(USER_AGENT)
                    .timeout(5000)
                    .get();
            Elements paragraphs = doc.select(".article_body p, .highLightSearchTarget p, #uamods p, .content--detail-main p, #cmsBody p, .znc p, article p");

            if (!paragraphs.isEmpty()) {
                List<String> pTexts = new ArrayList<>();
                for (Element p : paragraphs) {
                    // Ігноруємо порожні абзаци
                    if(!p.text().trim().isEmpty()) {
                        pTexts.add(p.text());
                    }
                }
                return String.join("\n\n", pTexts);
            } else {
                Elements allParagraphs = doc.select("p");
                List<String> allTexts = new ArrayList<>();
                for (Element p : allParagraphs) {
                    if(!p.text().trim().isEmpty()) {
                        allTexts.add(p.text());
                    }
                }
                return String.join("\n\n", allTexts);
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження повного тексту: " + e.getMessage());
            return "Не вдалося автоматично завантажити текст. Оригінальне посилання: " + articleUrl;
        }
    }
}