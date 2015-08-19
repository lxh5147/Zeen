package com.zeen.plagiarismchecker.impl;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class BinearySearchPerfTest {
    private static final int SIZE = 10000000;
    long[] values;

    @Before
    public void setup() {
        values = new long[SIZE];
        Random random = new Random();
        for (int i = 0; i < SIZE; ++i) {
            values[i] = random.nextLong();
        }
        Arrays.sort(values);
    }

    private static final int SEARCH_TIMES = 1000000;

    @Test
    public void searchHughArrayTest() {
        Random random = new Random();
        long value = -1;
        for (int i = 0; i < SEARCH_TIMES; i++) {
            value = values[random.nextInt(SIZE)];
            Arrays.binarySearch(values, value);
        }

        for (int i = 0; i < SEARCH_TIMES; i++) {
            value = random.nextLong();
            Arrays.binarySearch(values, value);
        }
    }
}
