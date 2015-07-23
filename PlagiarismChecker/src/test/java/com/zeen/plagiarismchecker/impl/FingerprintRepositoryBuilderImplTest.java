package com.zeen.plagiarismchecker.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zeen.plagiarismchecker.ContentAnalizer;
import com.zeen.plagiarismchecker.FingerprintRepository;

public class FingerprintRepositoryBuilderImplTest {
    @Test
    public void newFingerprintRepositoryBuilderImplTest() {
        FingerprintRepositoryBuilderImpl builder = new FingerprintRepositoryBuilderImpl();
        Assert.assertNull(builder.analizer);
        Assert.assertNull(builder.values);
        Assert.assertNull(builder.articleEntries);
        Assert.assertNull(builder.paragraphEntries);
        Assert.assertEquals(0, builder.size);
    }

    @Test
    public void buildTest() {
        FingerprintRepositoryBuilderImpl builder = new FingerprintRepositoryBuilderImpl();
        ContentAnalizer analizer = new SimpleContentAnalizer(
                new SimpleTokenizer());
        int capability = 1024;
        builder.start(analizer, capability);
        final List<String> paragraphes = Lists
                .newArrayList(
                        "this work uses deep learning to represent a reference. we can summurize our contributions as follows.",
                        "deep learning has attracted increasing interestes in recent studies on nlp. it has been widely applied to word embedding, language modeling.",
                        "it is still not clear if and how deep learning can help to detect repeated text. In this work, we design a mapping from a paragraph to a paragraph vector, which is used to detect copy&past text.");
        int paragraphId = 0;
        int referenceId = 0;
        for (String content : paragraphes) {
            builder.add(new ArticleRepositoryImpl.ParagraphImpl(referenceId,
                    paragraphId++, content));
        }

        FingerprintRepository fingerprintRepository = builder.build();
        Assert.assertNotNull(fingerprintRepository);
        paragraphId = 0;
        StringBuilder stringBuffer = new StringBuilder();
        for (String content : paragraphes) {
            Assert.assertEquals(
                    Sets.newHashSet(FingerprintRepositoryImpl
                            .newParagraphEntry(referenceId, paragraphId)),
                    Sets.newHashSet(fingerprintRepository
                            .getFingerprintEntries(FingerprintRepositoryImpl.newFingerprint(
                                    FingerprintRepositoryBuilderImpl.FINGERPRINT_BUILDER
                                            .getFingerprint(content, analizer,
                                                    stringBuffer)))));
            paragraphId++;
        }
    }
}
