package com.zeen.plagiarismchecker.impl;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class FingerprintBuilderPerfTest {
    FingerprintBuilder builder;
    String content;

    @Before
    public void setup() {
        builder = new FingerprintBuilder();
        content = "hi this is some tests; please check how long it taks. this will give some clues about how fast it is. let's do it now.";
    }

    private static int TEST_TIMES = 200000000;

    @Test
    public void buildHugeNumberOfFingerprintsTest() {
        Random random = new Random();
        for (int i = 0; i < TEST_TIMES; ++i) {
            builder.getFingerprint(content
                    + String.valueOf(random.nextInt(TEST_TIMES)));
        }
    }
}
