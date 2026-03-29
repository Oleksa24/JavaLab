package org.lab.javalab.controller;

import org.lab.javalab.dto.VocabularyDto;
import org.lab.javalab.model.Vocabulary;
import org.lab.javalab.service.DictionaryParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.lab.javalab.repository.VocabularyRepository;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
public class AppController {

    private final DictionaryParserService parserService;
    private final VocabularyRepository vocabularyRepository;

    public AppController(DictionaryParserService parserService, VocabularyRepository vocabularyRepository) {
        this.parserService = parserService;
        this.vocabularyRepository = vocabularyRepository;
    }
    @PostMapping("/add")
    public ResponseEntity<Vocabulary> addWord(@RequestBody VocabularyDto dto) {
        Vocabulary savedWord = parserService.fetchAndSaveWordInfo(dto.word);
        return ResponseEntity.ok(savedWord);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Vocabulary>> getAllWords() {
        return ResponseEntity.ok(vocabularyRepository.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWord(@PathVariable Long id) {
        vocabularyRepository.deleteById(id);
        return ResponseEntity.ok("Слово видалено");
    }
}
