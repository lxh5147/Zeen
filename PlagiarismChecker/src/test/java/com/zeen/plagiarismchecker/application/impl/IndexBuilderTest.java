package com.zeen.plagiarismchecker.application.impl;

import java.io.File;
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
import com.zeen.plagiarismchecker.Article;
import com.zeen.plagiarismchecker.FingerprintRepository;
import com.zeen.plagiarismchecker.Paragraph;
import com.zeen.plagiarismchecker.ParagraphEntry;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryTestUtil;
import com.zeen.plagiarismchecker.impl.ContentAnalyzerType;
import com.zeen.plagiarismchecker.impl.FingerprintRepositoryBuilderImpl;
import com.zeen.plagiarismchecker.impl.FingerprintRepositoryImpl;

public class IndexBuilderTest {

    @Before
    public void setup() throws IOException {
        ArticleRepositoryTestUtil.setupArticleRepository();
    }

    @After
    public void tearDown() {
        ArticleRepositoryTestUtil.tearDownArticleRepository();
    }

    @Test
    public void getIndexBuilderTest() throws ParseException, IOException {
        String indexRoot = "index";
        List<ContentAnalyzerType> contentAnalizersList = Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer);

        String[] args = { "--articleRepositoryFolders",
                Joiner.on(',').join(ArticleRepositoryTestUtil.FOLDERS),
                "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPath",
                indexRoot, "--capability", String.valueOf(10000) };
        IndexBuilder indexBuilder = IndexBuilder.getIndexBuilderWithArgs(args);
        Assert.assertNotNull(indexBuilder);
        Assert.assertEquals(10000, indexBuilder.capability);
        Assert.assertEquals("ArticleRepositoryImpl{folders=[ref1, ref2]}",
                indexBuilder.articleRepository.toString());
        Assert.assertEquals(2,
                indexBuilder.fingerprintRepositoryInfoList.size());

        for (int i = 0; i < contentAnalizersList.size(); ++i) {
            Assert.assertEquals(
                    contentAnalizersList.get(i),
                    indexBuilder.fingerprintRepositoryInfoList.get(i).getContentAnalyzerType());
            Assert.assertEquals(
                    Paths.get(indexRoot)
                            .resolve(contentAnalizersList.get(i).name())
                            .toFile(),
                    indexBuilder.fingerprintRepositoryInfoList.get(i).getIndexFile());
        }
    }

    public static void deleteIndex(String indexRoot,
            List<ContentAnalyzerType> contentAnalizersList) {
        // delete two index files,this also ensure only two indexes are
        // generated
        for (ContentAnalyzerType contentAnalizers : contentAnalizersList) {
            Paths.get(indexRoot).resolve(contentAnalizers.name()).toFile()
                    .delete();
        }
        new File(indexRoot).delete();
    }

    public static void setupIndex(String indexRoot,
            List<ContentAnalyzerType> contentAnalizersList) throws IOException,
            ParseException {
        String[] args = { "--articleRepositoryFolders",
                Joiner.on(',').join(ArticleRepositoryTestUtil.FOLDERS),
                "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPath",
                indexRoot, "--capability", String.valueOf(10000) };
        IndexBuilder.getIndexBuilderWithArgs(args).build();
    }

    @Test
    public void buildTest() throws ParseException, IOException {
        String indexRoot = "index";
        List<ContentAnalyzerType> contentAnalizersList = Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer);
        String[] args = { "--articleRepositoryFolders",
                Joiner.on(',').join(ArticleRepositoryTestUtil.FOLDERS),
                "--contentAnalyzers",
                Joiner.on(',').join(contentAnalizersList), "--indexPath",
                indexRoot, "--capability", String.valueOf(10000) };
        IndexBuilder indexBuilder = IndexBuilder.getIndexBuilderWithArgs(args);
        indexBuilder.build();

        StringBuilder stringBuffer = new StringBuilder();

        // verify that we have two finger repositories
        for (ContentAnalyzerType contentAnalizers : Lists
                .newArrayList(
                        ContentAnalyzerType.SimpleContentAnalizerWithSimpleTokenizer,
                        ContentAnalyzerType.BagOfWordsContentAnalizerWithOpenNLPTokenizer)) {
            FingerprintRepository fingerprintRepository = FingerprintRepositoryImpl
                    .load(Paths.get(indexRoot).resolve(contentAnalizers.name())
                            .toFile());
            for (Article article : indexBuilder.articleRepository.getArticles()) {
                for (Paragraph paragraph : article.getParagraphes()) {
                    List<ParagraphEntry> paragraphEntries = Lists
                            .newArrayList(fingerprintRepository
                                    .getFingerprintEntries(FingerprintRepositoryImpl
                                            .newFingerprint(FingerprintRepositoryBuilderImpl.FINGERPRINT_BUILDER.getFingerprint(
                                                    paragraph.getContent(),
                                                    contentAnalizers
                                                            .getContentAnalyzer(),
                                                    stringBuffer))));
                    Assert.assertEquals(1, paragraphEntries.size());
                    Assert.assertEquals(article.getId(), paragraphEntries
                            .get(0).getArticleId());
                    Assert.assertEquals(paragraph.getId(), paragraphEntries
                            .get(0).getParagraphId());
                }
            }
        }
        deleteIndex(indexRoot, contentAnalizersList);
    }
}
