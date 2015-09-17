package com.zeen.plagiarismchecker.application.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.zeen.plagiarismchecker.Article;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryImpl;

public class ArticleRepositoryBuilderTest {
    private static final String TEST_FILE_FOLDER = "testFiles";

    @Test
    public void getArticleRepositoryBuilderTest() throws ParseException,
            IOException {
        final String articleRepositoryFolder = "articles";

        String[] args = { "--pdfTextFileFolders", "testFiles",
                "--articleRepositoryFolder", articleRepositoryFolder,
                "--overwrite", "--lowercase" };

        ArticleRepositoryBuilder builder = ArticleRepositoryBuilder
                .getArticleRepositoryBuilderWithArgs(args);
        Assert.assertNotNull(builder);
        Assert.assertEquals(true, builder.overwrite);
        Assert.assertEquals(Paths.get(articleRepositoryFolder),
                builder.articleRepositoryFolder);
        Assert.assertEquals(Lists.newArrayList(Paths.get(TEST_FILE_FOLDER)),
                builder.pdfTextFileFolders);
    }

    @Test
    public void buildTest() throws ParseException, IOException {
        final String articleRepositoryFolder = "articles";
        String[] args = { "--pdfTextFileFolders", "testFiles",
                "--articleRepositoryFolder", articleRepositoryFolder,
                "--overwrite", "--lowercase" };

        ArticleRepositoryBuilder builder = ArticleRepositoryBuilder
                .getArticleRepositoryBuilderWithArgs(args);
        builder.build();
        ArticleRepositoryImpl referenceRepository = new ArticleRepositoryImpl(
                Lists.newArrayList(Paths.get(articleRepositoryFolder)));
        List<String> fileIds = Lists.newArrayList();
        ArticleRepositoryImpl
                .getFiles(Paths.get(TEST_FILE_FOLDER))
                .forEachRemaining(
                        path -> {
                            String fileName = path.toFile().getName();
                            if (Files.getFileExtension(fileName).equals("txt")) {
                                String fileId = Files
                                        .getNameWithoutExtension(fileName);
                                Article article = referenceRepository
                                        .getArticle(Integer.valueOf(fileId));
                                // check all paragraph is lower cased
                                Lists.newArrayList(article.getParagraphs())
                                        .forEach(paragraph -> {
                                                    Assert.assertEquals(
                                                            paragraph
                                                                    .getContent()
                                                                    .toLowerCase(),
                                                            paragraph
                                                                    .getContent());
                                        });
                                Assert.assertNotNull(article);
                                fileIds.add(fileId);
                            }
                        });

        fileIds.forEach(fileId -> {
            Paths.get(articleRepositoryFolder).resolve(fileId).toFile()
                    .delete();
        });
        Paths.get(articleRepositoryFolder).toFile().delete();
    }
   
}
