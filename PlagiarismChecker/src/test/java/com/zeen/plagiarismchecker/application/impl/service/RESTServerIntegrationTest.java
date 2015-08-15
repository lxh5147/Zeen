package com.zeen.plagiarismchecker.application.impl.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.application.impl.IndexBuilderTest;

import com.zeen.plagiarismchecker.impl.ArticleRepositoryTestUtil;
import com.zeen.plagiarismchecker.impl.ContentAnalyzerType;

public class RESTServerIntegrationTest {
    @Before
    public void setup() throws IOException {
        ArticleRepositoryTestUtil.setupArticleRepository();
    }

    @After
    public void tearDown() {
        ArticleRepositoryTestUtil.tearDownArticleRepository();
    }

    @Test
    public void checkWithRESTClientTest() throws Exception {
        String indexRoot = "index";
        final List<ContentAnalyzerType> contentAnalizersList = Lists
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
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable server = () -> {
            try {
                RESTServer.main(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        executor.execute(server);
        while (!RESTServer.started) {
            Thread.sleep(0);
        }
        final boolean[][] done = new boolean[ArticleRepositoryTestUtil.ARTICLES.length][];
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            done[i] = new boolean[ArticleRepositoryTestUtil.ARTICLES[i].length];
            for (int j = 0; j < ArticleRepositoryTestUtil.ARTICLES[i].length; ++j) {
                done[i][j] = false;
            }
        }
        // now we have several clients
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            for (int j = 0; j < ArticleRepositoryTestUtil.ARTICLES[i].length; ++j) {
                final String paragraphContent = ArticleRepositoryTestUtil.ARTICLES[i][j];
                final int articleId = i;
                final int paragraphId = j;
                Runnable client = () -> {
                    // send request to server to check
                    try {
                        HttpClient httpClient = new HttpClient();
                        httpClient.start();
                        ContentResponse response = httpClient
                                .newRequest("localhost", 8080)
                                .method(HttpMethod.GET).path("/check")
                                .timeout(5, TimeUnit.SECONDS)
                                .param("paragraph", paragraphContent).send();

                        Assert.assertEquals(
                                Lists.newArrayList(new CheckResult(
                                        articleId,
                                        paragraphId,
                                        ArticleRepositoryTestUtil.ARTICLES[articleId][paragraphId],
                                        contentAnalizersList)), CheckResult
                                        .getCheckResults(response
                                                .getContentAsString()));

                        httpClient.stop();
                        done[articleId][paragraphId] = true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
                executor.execute(client);
            }
        }

        executor.shutdown();
        while (!allDone(done)) {
            Thread.sleep(0);
        }
        executor.awaitTermination(0, TimeUnit.MICROSECONDS);
        IndexBuilderTest.deleteIndex(indexRoot, contentAnalizersList);
    }

    private boolean allDone(boolean[][] done) {
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            for (int j = 0; j < ArticleRepositoryTestUtil.ARTICLES[i].length; ++j) {
                if (!done[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    public void checkDocumentWithRESTClientTest() throws Exception {
        String indexRoot = "index";
        final List<ContentAnalyzerType> contentAnalizersList = Lists
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
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable server = () -> {
            try {
                RESTServer.main(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        executor.execute(server);
        while (!RESTServer.started) {
            Thread.sleep(0);
        }
        final boolean[] done = new boolean[ArticleRepositoryTestUtil.ARTICLES.length];
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            done[i] = false;
        }
        // now we have several clients
        for (int i = 0; i < ArticleRepositoryTestUtil.ARTICLES.length; ++i) {
            final int articleId = i;
            Runnable client = () -> {
                // send request to server to check
                try {
                    HttpClient httpClient = new HttpClient();
                    httpClient.start();
                    ContentResponse response = httpClient
                            .newRequest("localhost", 8080)
                            .method(HttpMethod.POST)
                            .path("/checkDocument")
                            .timeout(5, TimeUnit.SECONDS)
                            .param("document",
                                    RESTServerTest
                                            .getDocumentContent(ArticleRepositoryTestUtil.ARTICLES[articleId]))
                            .send();
                    List<ParagraphCheckResult> expected = Lists.newArrayList();
                    for (int j = 0; j < ArticleRepositoryTestUtil.ARTICLES[articleId].length; ++j) {
                        ParagraphCheckResult paragraphCheckResult = new ParagraphCheckResult();
                        paragraphCheckResult
                                .setParagraphContentToCheck(ArticleRepositoryTestUtil.ARTICLES[articleId][j]);
                        paragraphCheckResult
                                .setCheckResults(Lists
                                        .newArrayList(new CheckResult(
                                                articleId,
                                                j,
                                                ArticleRepositoryTestUtil.ARTICLES[articleId][j],
                                                contentAnalizersList)));
                        expected.add(paragraphCheckResult);
                    }

                    // System.out.println(response.getContentAsString());

                    Assert.assertEquals(expected, Lists
                            .newArrayList(ParagraphCheckResult
                                    .getParagraphCheckResult(response
                                            .getContentAsString())));

                    httpClient.stop();
                    done[articleId] = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            executor.execute(client);
        }

        executor.shutdown();
        while (!allDone(done)) {
            Thread.sleep(0);
        }
        executor.awaitTermination(0, TimeUnit.MICROSECONDS);
        IndexBuilderTest.deleteIndex(indexRoot, contentAnalizersList);
    }

    private boolean allDone(boolean[] done) {
        for (int i = 0; i < done.length; ++i) {
            if (!done[i]) {
                return false;
            }
        }
        return true;
    }
}
