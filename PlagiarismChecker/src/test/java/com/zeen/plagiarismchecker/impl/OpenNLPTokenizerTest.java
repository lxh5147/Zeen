package com.zeen.plagiarismchecker.impl;

import java.io.IOException;
import java.util.List;

import opennlp.tools.util.InvalidFormatException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class OpenNLPTokenizerTest {
    @Test
    public void tokenizationTest() throws InvalidFormatException, IOException {
        Tokenizer tokenizer = new OpenNLPTokenizer();
        List<CharSequence> tokens = Lists.newArrayList(tokenizer
                .split("Hi. How are you? This is Mike."));
        List<CharSequence> expected = Lists.newArrayList("Hi", ".", "How",
                "are", "you", "?", "This", "is", "Mike", ".");
        Assert.assertEquals(expected, tokens);
    }
}
