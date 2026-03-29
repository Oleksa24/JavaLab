package org.lab.javalab.repository;

import org.lab.javalab.model.Article;
import org.lab.javalab.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
