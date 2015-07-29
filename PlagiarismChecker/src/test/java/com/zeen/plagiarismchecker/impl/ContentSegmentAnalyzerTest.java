package com.zeen.plagiarismchecker.impl;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.ContentAnalyzer;

public class ContentSegmentAnalyzerTest {
    @SuppressWarnings("unchecked")
    @Test
    public void getCheckPointsTest() {
        ContentAnalyzer contentAnalizer = new ContentSegmentAnalyzer(
                new SimpleTokenizer(), new SimpleSegmentSplitter(), 10, 12);
        // # is removed by tokenizer
        Assert.assertEquals(
                Lists.newArrayList(Lists.newArrayList("1", "2", "3", "4", "5",
                        "6", "7", "8", "21", "22", "23", "24"), Lists
                        .newArrayList("21", "22", "23", "24", "25", "26", "27",
                                "28", "29", "210"), Lists.newArrayList("31",
                        "32")),
                contentAnalizer
                        .analyze("!#1 2 3 4 5 6 7 8. #21 22 23 24 25 26 27 28 29 210! #31 32"));
    }
}
