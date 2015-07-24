package com.zeen.plagiarismchecker.impl;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class BinearySearchWithIndexTest {
    @Test
    public void sortTest() {
        Assert.assertEquals(0,
                new FingerprintRepositoryImpl.BinearySearchWithIndex(
                        new long[] { 3, 1, 2 }, new int[] { 1, 2, 0 }, 3)
                        .search(1));

        Assert.assertEquals(1,
                new FingerprintRepositoryImpl.BinearySearchWithIndex(
                        new long[] { 3, 1, 2 }, new int[] { 1, 2, 0 }, 3)
                        .search(2));
        Assert.assertEquals(2,
                new FingerprintRepositoryImpl.BinearySearchWithIndex(
                        new long[] { 3, 1, 2 }, new int[] { 1, 2, 0 }, 3)
                        .search(3));

        Assert.assertEquals(-4,
                new FingerprintRepositoryImpl.BinearySearchWithIndex(
                        new long[] { 3, 1, 2 }, new int[] { 1, 2, 0 }, 3)
                        .search(4));
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
        FingerprintRepositoryImpl.BinearySearchWithIndex searchWithIndex = new FingerprintRepositoryImpl.BinearySearchWithIndex(
                values, index, size);
        for (int i = 0; i < size; ++i) {
            int pos = searchWithIndex.search(values[i]);
            Assert.assertTrue(pos >= 0);
            Assert.assertEquals(values[i], values[index[pos]]);
        }
    }
}
