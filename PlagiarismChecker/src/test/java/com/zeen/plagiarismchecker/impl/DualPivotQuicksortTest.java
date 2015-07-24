package com.zeen.plagiarismchecker.impl;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class DualPivotQuicksortTest {

    @Test
    public void sortTest() {

        Assert.assertArrayEquals(new int[] { 1, 2, 0 },
                new FingerprintRepositoryBuilderImpl.DualPivotQuicksort(
                        new long[] { 3, 1, 2 }, 3).sort());
        Assert.assertArrayEquals(new int[] { 1, 4, 0, 2, 3 },
                new FingerprintRepositoryBuilderImpl.DualPivotQuicksort(
                        new long[] { 3, 1, 4, 8, 2 }, 5).sort());
    }

    @Test
    public void sortWithRandomSamplesTest() {
        Random random = new Random();
        int size = 10240;
        long[] values = new long[size + 100];
        for (int i = 0; i < size; ++i) {
            values[i] = random.nextLong();
        }
        int[] index = new FingerprintRepositoryBuilderImpl.DualPivotQuicksort(
                values, size).sort();
        for (int i = 0; i < size - 1; ++i) {
            Assert.assertTrue(values[index[i]] <= values[index[i + 1]]);
        }
    }
}
