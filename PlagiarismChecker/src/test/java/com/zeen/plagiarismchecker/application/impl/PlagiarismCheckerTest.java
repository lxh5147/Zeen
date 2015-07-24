package com.zeen.plagiarismchecker.application.impl;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryTestUtil;
import com.zeen.plagiarismchecker.impl.ContentAnalyzerType;
import com.zeen.plagiarismchecker.impl.FingerprintRepositoryImpl;

public class PlagiarismCheckerTest {
    @Before
    public void setup() throws IOException {
        ArticleRepositoryTestUtil.setupArticleRepository();
    }

    @After
    public void tearDown() {
        ArticleRepositoryTestUtil.tearDownArticleRepository();
    }

    @Test
    public void getPlagiarismCheckerTest() throws ParseException, IOException {
        String indexRoot = "index";
        List<ContentAnalyzerType> contentAnalizersList = Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer);
        IndexBuilderTest.setupIndex(indexRoot, contentAnalizersList);
        String[] args = { "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPath",
                indexRoot };
        List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList = Lists
                .newArrayList();
        contentAnalizersList.forEach(item -> {
            fingerprintRepositoryInfoList.add(new FingerprintRepositoryInfo(
                    item, Paths.get(indexRoot).resolve(item.name()).toFile()));
        });
        Assert.assertEquals(
                new PlagiarismChecker(fingerprintRepositoryInfoList),
                PlagiarismChecker.getPlagiarismCheckerWithArgs(args));
        IndexBuilderTest.deleteIndex(indexRoot, contentAnalizersList);
    }

    @Test
    public void checkTest() throws IOException, ParseException {
        String indexRoot = "index";
        List<ContentAnalyzerType> contentAnalizersList = Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer);
        IndexBuilderTest.setupIndex(indexRoot, contentAnalizersList);
        String[] args = { "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPath",
                indexRoot };
        PlagiarismChecker plagiarismChecker = PlagiarismChecker
                .getPlagiarismCheckerWithArgs(args);
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            for (int j = 0; j < ArticleRepositoryTestUtil.ARTICLES[i].length; ++j) {
                Assert.assertEquals(
                        contentAnalizersList.size(),
                        Lists.newArrayList(
                                plagiarismChecker
                                        .check(ArticleRepositoryTestUtil.ARTICLES[i][j]))
                                .size());

                for (int k = 0; k < contentAnalizersList.size(); ++k) {
                    Assert.assertEquals(
                            contentAnalizersList.get(k),
                            Lists.newArrayList(
                                    plagiarismChecker
                                            .check(ArticleRepositoryTestUtil.ARTICLES[i][j]))
                                    .get(k).getKey());
                    Assert.assertEquals(
                            Sets.newHashSet(FingerprintRepositoryImpl
                                    .newParagraphEntry(i, j)),
                            Sets.newHashSet(Lists
                                    .newArrayList(
                                            plagiarismChecker
                                                    .check(ArticleRepositoryTestUtil.ARTICLES[i][j]))
                                    .get(k).getValue()));

                }
            }
        }
        IndexBuilderTest.deleteIndex(indexRoot, contentAnalizersList);
    }
}
