package com.zeen.plagiarismchecker.impl;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.ContentAnalyzer;

public class SimpleContentAnalyzerTest {
    @Test
    public void getCheckPointsTest() {
        ContentAnalyzer contentAnalizer = new SimpleContentAnalyzer(
                new SimpleTokenizer());
        // keep case
        Assert.assertEquals(1,
                Lists.newArrayList(contentAnalizer.analyze("This's me."))
                        .size());

        Assert.assertEquals(
                Lists.newArrayList("This", "s", "me"),
                Lists.newArrayList(contentAnalizer.analyze("This's me.")
                        .iterator().next()));
        // numbers
        Assert.assertEquals(1,
                Lists.newArrayList(contentAnalizer.analyze("number 123? yes!"))
                        .size());
        Assert.assertEquals(
                Lists.newArrayList("number", "123", "yes"),
                Lists.newArrayList(contentAnalizer.analyze("number 123? yes!")
                        .iterator().next()));
    }
}
