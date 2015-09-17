package com.zeen.plagiarismchecker.impl;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.collect.Lists;

public class SimpleSegmentSplitterTest {

    @Test
    public void splitTest() {
        SimpleSegmentSplitter simpleSegmentSplitter = new SimpleSegmentSplitter();
        // remove empty segments, and trim leading and ending blanks
        Assert.assertEquals(
                Lists.newArrayList("is this yours", "yes,that is nice then",
                        "test again"),
                Lists.newArrayList(simpleSegmentSplitter
                        .split("is this yours?yes,that is nice then. test again!.?")));
    }
}
