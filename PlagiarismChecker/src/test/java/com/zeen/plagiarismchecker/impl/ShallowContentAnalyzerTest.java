package com.zeen.plagiarismchecker.impl;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.ContentAnalyzer;

public class ShallowContentAnalyzerTest {
    @Test
    public void getCheckPointsTest() {
        ContentAnalyzer contentAnalizer = new ShallowContentAnalyzer(
                new SimpleTokenizer());
        // lower case and stop words removal
        Assert.assertEquals(1,
                Lists.newArrayList(contentAnalizer.analyze("This'S me."))
                        .size());
        Assert.assertEquals(
                Lists.newArrayList("s"),
                Lists.newArrayList(contentAnalizer.analyze("This'S me.")
                        .iterator().next()));
        // numbers
        Assert.assertEquals(1,
                Lists.newArrayList(contentAnalizer.analyze("number 123?"))
                        .size());
        Assert.assertEquals(
                Lists.newArrayList("number", "123"),
                Lists.newArrayList(contentAnalizer.analyze("number 123?")
                        .iterator().next()));
        // stemming
        Assert.assertEquals(1,
                Lists.newArrayList(contentAnalizer.analyze("good tests!"))
                        .size());
        Assert.assertEquals(
                Lists.newArrayList("good", "test"),
                Lists.newArrayList(contentAnalizer.analyze("good tests!")
                        .iterator().next()));
    }
}
