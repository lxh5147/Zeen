package com.zeen.plagiarismchecker.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zeen.plagiarismchecker.ContentAnalyzer;
import com.zeen.plagiarismchecker.FingerprintRepository;
import com.zeen.plagiarismchecker.Paragraph;

public class FingerprintRepositoryBuilderImplTest {
    @Test
    public void newFingerprintRepositoryBuilderImplTest() {
        FingerprintRepositoryBuilderImpl builder = new FingerprintRepositoryBuilderImpl();
        Assert.assertNull(builder.analyzer);
        Assert.assertNull(builder.values);
        Assert.assertNull(builder.articleEntries);
        Assert.assertNull(builder.paragraphEntries);
        Assert.assertEquals(0, builder.size);
    }

    @Test
    public void buildTest() {
        FingerprintRepositoryBuilderImpl builder = new FingerprintRepositoryBuilderImpl();
        ContentAnalyzer analizer = new SimpleContentAnalyzer(
                new SimpleTokenizer());
        int capability = 1024;
        builder.start(analizer, capability);
        final List<String> paragraphContentList = Lists
                .newArrayList(
                        "this work uses deep learning to represent a reference. we can summurize our contributions as follows.",
                        "deep learning has attracted increasing interestes in recent studies on nlp. it has been widely applied to word embedding, language modeling.",
                        "it is still not clear if and how deep learning can help to detect repeated text. In this work, we design a mapping from a paragraph to a paragraph vector, which is used to detect copy&past text.");
        int paragraphId = 0;
        int referenceId = 0;
        for (String content : paragraphContentList) {
            builder.add(new ArticleRepositoryImpl.ParagraphImpl(referenceId,
                    paragraphId++, content));
        }

        FingerprintRepository fingerprintRepository = builder.build();
        Assert.assertNotNull(fingerprintRepository);
        paragraphId = 0;
        StringBuilder stringBuilder = new StringBuilder();
        long[] fingerprintBuffer = new long[1];

        for (String content : paragraphContentList) {
            FingerprintRepositoryBuilderImpl.FINGERPRINT_BUILDER
                    .buildFingerprints(
                            Lists.newArrayList(analizer.analyze(content)),
                            stringBuilder, fingerprintBuffer);
            Assert.assertEquals(

            Sets.newHashSet(FingerprintRepositoryImpl.newParagraphEntry(
                    referenceId, paragraphId)), Sets
                    .newHashSet(fingerprintRepository
                            .getFingerprintEntries(FingerprintRepositoryImpl
                                    .newFingerprint(fingerprintBuffer[0]))));
            paragraphId++;
        }
    }

    @Test
    public void buildWithAddingMultipleParagraphsTest() {
        FingerprintRepositoryBuilderImpl builder = new FingerprintRepositoryBuilderImpl();
        ContentAnalyzer analizer = new SimpleContentAnalyzer(
                new SimpleTokenizer());
        int capability = 1024;
        builder.start(analizer, capability);
        final List<String> paragraphContentList = Lists
                .newArrayList(
                        "this work uses deep learning to represent a reference. we can summurize our contributions as follows.",
                        "deep learning has attracted increasing interestes in recent studies on nlp. it has been widely applied to word embedding, language modeling.",
                        "it is still not clear if and how deep learning can help to detect repeated text. In this work, we design a mapping from a paragraph to a paragraph vector, which is used to detect copy&past text.");
        int paragraphId = 0;
        int referenceId = 0;
        List<Paragraph> paragraphes = Lists.newArrayList();

        for (String content : paragraphContentList) {
            paragraphes.add(new ArticleRepositoryImpl.ParagraphImpl(referenceId,
                    paragraphId++, content));
        }

        builder.add(paragraphes, 2);

        FingerprintRepository fingerprintRepository = builder.build();
        Assert.assertNotNull(fingerprintRepository);
        paragraphId = 0;
        StringBuilder stringBuilder = new StringBuilder();
        long[] fingerprintBuffer = new long[1];

        for (String content : paragraphContentList) {
            FingerprintRepositoryBuilderImpl.FINGERPRINT_BUILDER
                    .buildFingerprints(
                            Lists.newArrayList(analizer.analyze(content)),
                            stringBuilder, fingerprintBuffer);
            Assert.assertEquals(

            Sets.newHashSet(FingerprintRepositoryImpl.newParagraphEntry(
                    referenceId, paragraphId)), Sets
                    .newHashSet(fingerprintRepository
                            .getFingerprintEntries(FingerprintRepositoryImpl
                                    .newFingerprint(fingerprintBuffer[0]))));
            paragraphId++;
        }
    }
}
