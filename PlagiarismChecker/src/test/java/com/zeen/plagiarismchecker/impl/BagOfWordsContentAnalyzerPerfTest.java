package com.zeen.plagiarismchecker.impl;

import org.junit.Test;

import com.zeen.plagiarismchecker.ContentAnalyzer;

public class BagOfWordsContentAnalyzerPerfTest {
    // 1M articles, each with 200 paragraphs, will be 10000000*200
    private static final int TEST_TIMES = 10000;
    private static final String PARAGRAPH = "Today is one of great days. Tomorrow is also great DAY. we should be more caleful for per testing. we can generate multiple check point lists for one paragraph. what really matters are the number of fingerprints.";

    @Test
    public void analyzeManyParagraphesWithSimpleTokenizerTest() {
        ContentAnalyzer contentAnalizer = new BagOfWordsContentAnalyzer(
                new ShallowContentAnalyzer(new SimpleTokenizer()));
        // fine, since there is no cache
        for (int i = 0; i < TEST_TIMES; ++i) {
            contentAnalizer.analyze(PARAGRAPH);
        }
    }

    @Test
    public void analyzeManyParagraphesWithOpenNLPTokenizerTest() {
        ContentAnalyzer contentAnalizer = new BagOfWordsContentAnalyzer(
                new ShallowContentAnalyzer(new OpenNLPTokenizer()));
        // fine, since there is no cache
        for (int i = 0; i < TEST_TIMES; ++i) {
            contentAnalizer.analyze(PARAGRAPH);
        }
    }
}
