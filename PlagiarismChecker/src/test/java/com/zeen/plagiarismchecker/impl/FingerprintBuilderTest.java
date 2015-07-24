package com.zeen.plagiarismchecker.impl;

import org.junit.Assert;
import org.junit.Test;

public class FingerprintBuilderTest {
    @Test
    public void buildFingerprintTest() {
        FingerprintBuilder builder = new FingerprintBuilder();
        Assert.assertEquals(-791731574570693279L,
                builder.getFingerprint("hi this is some test"));
        Assert.assertEquals(1280403888826363364L,
                builder.getFingerprint("hi this is some tests"));
    }
}
