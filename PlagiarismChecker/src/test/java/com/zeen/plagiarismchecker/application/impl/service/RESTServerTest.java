package com.zeen.plagiarismchecker.application.impl.service;

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
import com.zeen.plagiarismchecker.application.impl.FingerprintRepositoryInfo;
import com.zeen.plagiarismchecker.application.impl.IndexBuilderTest;
import com.zeen.plagiarismchecker.application.impl.PlagiarismChecker;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryImpl;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryTestUtil;
import com.zeen.plagiarismchecker.impl.ContentAnalyzerType;

public class RESTServerTest {
    @Before
    public void setup() throws IOException {
        ArticleRepositoryTestUtil.setupArticleRepository();
    }

    @After
    public void tearDown() {
        ArticleRepositoryTestUtil.tearDownArticleRepository();
    }

    @Test
    public void setupContextTest() throws ParseException, IOException {
        String indexRoot = "index";
        List<ContentAnalyzerType> contentAnalizersList = Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer);
        IndexBuilderTest.setupIndex(indexRoot, contentAnalizersList);

        String[] args = { "--articleRepositoryFolders",
                Joiner.on(',').join(ArticleRepositoryTestUtil.FOLDERS),
                "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPaths",
                indexRoot };
        PlagiarismCheckerService.setupContext(args);
        Assert.assertEquals(
                new ArticleRepositoryImpl(Lists.newArrayList(Lists
                        .newArrayList(ArticleRepositoryTestUtil.FOLDERS)
                        .stream().map(folder -> {
                            return Paths.get(folder);
                        }).iterator())),
                PlagiarismCheckerService.Context.ARTICLE_REPOSITORY);
        Assert.assertEquals(1, PlagiarismCheckerService.Context.CHECKERS.size());
        List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList = Lists
                .newArrayList();
        contentAnalizersList.forEach(item -> {
            fingerprintRepositoryInfoList.add(new FingerprintRepositoryInfo(
                    item, Paths.get(indexRoot).resolve(item.name()).toFile()));
        });
        Assert.assertEquals(
                new PlagiarismChecker(fingerprintRepositoryInfoList),
                PlagiarismCheckerService.Context.CHECKERS.get(0));
        IndexBuilderTest.deleteIndex(indexRoot, contentAnalizersList);
    }

    @Test
    public void plagiarismCheckeServiceCheckTest() throws IOException,
            ParseException {
        String indexRoot = "index";
        List<ContentAnalyzerType> contentAnalizersList = Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer);
        IndexBuilderTest.setupIndex(indexRoot, contentAnalizersList);

        String[] args = { "--articleRepositoryFolders",
                Joiner.on(',').join(ArticleRepositoryTestUtil.FOLDERS),
                "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPaths",
                indexRoot };
        PlagiarismCheckerService.setupContext(args);
        PlagiarismCheckerService plagiarismCheckeService = new PlagiarismCheckerService();
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            for (int j = 0; j < ArticleRepositoryTestUtil.ARTICLES[i].length; ++j) {
                Assert.assertEquals(
                        Lists.newArrayList(new CheckResult(
                                i, j, ArticleRepositoryTestUtil.ARTICLES[i][j],
                                contentAnalizersList)),
                        Lists.newArrayList(plagiarismCheckeService
                                .check(ArticleRepositoryTestUtil.ARTICLES[i][j])));
            }
        }
        IndexBuilderTest.deleteIndex(indexRoot, contentAnalizersList);
    }

    @Test
    public void plagiarismCheckeServiceCheckDocumentTest() throws IOException,
            ParseException {
        String indexRoot = "index";
        List<ContentAnalyzerType> contentAnalizersList = Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer);
        IndexBuilderTest.setupIndex(indexRoot, contentAnalizersList);

        String[] args = { "--articleRepositoryFolders",
                Joiner.on(',').join(ArticleRepositoryTestUtil.FOLDERS),
                "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPaths",
                indexRoot, "--caseSensitive" };
        PlagiarismCheckerService.setupContext(args);
        PlagiarismCheckerService plagiarismCheckeService = new PlagiarismCheckerService();
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            final String documentContent = getDocumentContent(ArticleRepositoryTestUtil.ARTICLES[i]);
            List<ParagraphCheckResult> expected = Lists.newArrayList();
            for (int j = 0; j < ArticleRepositoryTestUtil.ARTICLES[i].length; ++j) {
                ParagraphCheckResult paragraphCheckResult = new ParagraphCheckResult();
                paragraphCheckResult
                        .setParagraphContentToCheck(ArticleRepositoryTestUtil.ARTICLES[i][j]);
                paragraphCheckResult.setCheckResults(Lists
                        .newArrayList(new CheckResult(
                                i, j, ArticleRepositoryTestUtil.ARTICLES[i][j],
                                contentAnalizersList)));
                expected.add(paragraphCheckResult);

            }
            Assert.assertEquals(expected, Lists
                    .newArrayList(plagiarismCheckeService
                            .checkDocument(documentContent)));

        }
        IndexBuilderTest.deleteIndex(indexRoot, contentAnalizersList);
    }

    static String getDocumentContent(String[] paragraphs) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paragraphs.length; ++i) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(paragraphs[i]);
        }
        return builder.toString();
    }
}
