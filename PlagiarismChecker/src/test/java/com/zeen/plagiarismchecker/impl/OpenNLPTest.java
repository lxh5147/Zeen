package com.zeen.plagiarismchecker.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class OpenNLPTest {
    @Test
    public void tokenizationTest() throws InvalidFormatException, IOException {
        InputStream is = this.getClass().getResourceAsStream("/en-token.bin");
        TokenizerModel model = new TokenizerModel(is);
        Tokenizer tokenizer = new TokenizerME(model);
        List<String> tokens = Lists.newArrayList(tokenizer
                .tokenize("Hi. How are you? This is Mike."));
        List<String> expected = Lists.newArrayList("Hi", ".", "How", "are",
                "you", "?", "This", "is", "Mike", ".");
        Assert.assertEquals(expected, tokens);
        // no need to close input stream, which is automatically closed after
        // the model is loaded
    }

    @Test
    public void sentenceDetectTest() throws InvalidFormatException, IOException {
        String paragraph = "Hi. How are you? This is Mike.";
        // always start with a model, a model is learned from training data
        InputStream is = this.getClass().getResourceAsStream("/en-sent.bin");
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);
        List<String> sentences = Lists.newArrayList(sdetector
                .sentDetect(paragraph));
        List<String> expected = Lists.newArrayList("Hi. How are you?",
                "This is Mike.");
        Assert.assertEquals(expected, sentences);
    }

    @Test
    public void sentenceDetectCaseTest() throws InvalidFormatException,
            IOException {
        String paragraph = "Table 31 Pearson’s Correlation Coefficients involving buckoff and number of outs across all datasets 57 The sport of bull riding has become the fastest growing professional sporting event in the United States (PBR, 2008)";
        InputStream is = this.getClass().getResourceAsStream("/en-sent.bin");
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);
        List<String> sentences = Lists.newArrayList(sdetector
                .sentDetect(paragraph));
        Assert.assertEquals(1, sentences.size());
    }


    @Test
    public void stemmingTest() {
        Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH, 1);
        Assert.assertEquals("like", stemmer.stem("likes"));
        Assert.assertEquals("eat", stemmer.stem("eating"));
        Assert.assertEquals("love", stemmer.stem("lovely"));
        // no morphology
        Assert.assertEquals("ate", stemmer.stem("ate"));
    }

    @Test
    public void posTagTest() throws InvalidFormatException, IOException {
        InputStream is = this.getClass().getResourceAsStream(
                "/en-pos-maxent.bin");
        POSModel model = new POSModel(is);
        POSTaggerME tagger = new POSTaggerME(model);
        List<String> tags = Lists.newArrayList(tagger.tag(new String[] { "Hi",
                ".", "How", "are", "you", "?", "This", "is", "Mike", "." }));
        Assert.assertEquals(Lists
                .newArrayList("UH,.,WRB,VBP,PRP,.,DT,VBZ,NNP,.".split(",")),
                tags);
    }
}
