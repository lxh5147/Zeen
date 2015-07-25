package com.zeen.plagiarismchecker.impl;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
public class DualPivotQuicksortPerfTest {
    private static final int SIZE = 200000000;
    long[] values;

    @Before
    public void setup() {
        values = new long[SIZE];
        Random random = new Random();
        for (int i = 0; i < SIZE; ++i) {
            values[i] = random.nextLong();
        }
    }

    @Test
    public void sortHugeArrayTest() {
        new FingerprintRepositoryBuilderImpl.DualPivotQuicksort(values, SIZE)
                .sort();
    }
}
