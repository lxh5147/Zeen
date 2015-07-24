package com.zeen.plagiarismchecker;

public interface ArticleRepository {
    Iterable<Article> getArticles();

    Article getArticle(int id);
}
