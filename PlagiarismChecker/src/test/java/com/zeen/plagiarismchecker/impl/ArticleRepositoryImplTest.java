package com.zeen.plagiarismchecker.impl;

import java.io.IOException;

import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.Article;

public class ArticleRepositoryImplTest {

    @Before
    public void setup() throws IOException {
        ArticleRepositoryTestUtil.setupArticleRepository();
    }

    @After
    public void tearDown() {
        ArticleRepositoryTestUtil.tearDownArticleRepository();
    }

    @Test
    public void readReferencesTest() {        
        ArticleRepositoryImpl referenceRepository = new ArticleRepositoryImpl(
                Lists.newArrayList(Lists.newArrayList(ArticleRepositoryTestUtil.FOLDERS).stream().map( folder ->{
                    return Paths.get(folder); 
                }).iterator()));
        // load every reference into memory
        List<Article> references = Lists.newArrayList(referenceRepository
                .getArticles());
        Assert.assertEquals(ArticleRepositoryTestUtil.ARTICLES.length,
                references.size());
        for (int i = 0; i < references.size(); ++i) {
            Article reference = references.get(i);
            Assert.assertEquals(i, reference.getId());
            List<String> paragraphs = Lists.newArrayList();
            reference.getParagraphs().forEach(paragraph -> {
                paragraphs.add(paragraph.getContent());
            });
            Assert.assertEquals(
                    Lists.newArrayList(ArticleRepositoryTestUtil.ARTICLES[i]),
                    paragraphs);
        }
    }

    @Test
    public void getReferenceTest() {
        ArticleRepositoryImpl referenceRepository = new ArticleRepositoryImpl(
                Lists.newArrayList(Lists.newArrayList(ArticleRepositoryTestUtil.FOLDERS).stream().map( folder ->{
                    return Paths.get(folder); 
                }).iterator()));
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            Article reference = referenceRepository.getArticle(i);
            Assert.assertEquals(i, reference.getId());
            List<String> paragraphs = Lists.newArrayList();
            reference.getParagraphs().forEach(paragraph -> {
                paragraphs.add(paragraph.getContent());
            });
            Assert.assertEquals(
                    Lists.newArrayList(ArticleRepositoryTestUtil.ARTICLES[i]),
                    paragraphs);
        }
    }

}
