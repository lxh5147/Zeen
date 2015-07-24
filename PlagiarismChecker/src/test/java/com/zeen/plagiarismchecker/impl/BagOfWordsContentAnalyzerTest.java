package com.zeen.plagiarismchecker.impl;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.ContentAnalyzer;

public class BagOfWordsContentAnalyzerTest {

    @Test
    public void getCheckPointsTest() {
        ContentAnalyzer contentAnalizer = new BagOfWordsContentAnalyzer(
                new ShallowContentAnalyzer(new SimpleTokenizer()));
        // lower case and stop words removal
        Assert.assertEquals(
                Lists.newArrayList("day:2", "great:2", "one:1", "today:1",
                        "tomorrow:1"),
                Lists.newArrayList(contentAnalizer
                        .getCheckPoints("Today is one of great days. Tomorrow is also great DAY.")));
    }

}
