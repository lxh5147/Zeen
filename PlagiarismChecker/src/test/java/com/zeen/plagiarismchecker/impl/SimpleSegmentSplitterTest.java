package com.zeen.plagiarismchecker.impl;

import jersey.repackaged.com.google.common.collect.Lists;
import junit.framework.Assert;

import org.junit.Test;

public class SimpleSegmentSplitterTest {

    @Test
    public void splitTest() {
        SimpleSegmentSplitter simpleSegmentSplitter = new SimpleSegmentSplitter();
        Assert.assertEquals(
                Lists.newArrayList("is this yours", "yes,that is nice then",
                        "test again"),
                Lists.newArrayList(simpleSegmentSplitter
                        .split("is this yours?yes,that is nice then. test again!.?")));
    }
}
