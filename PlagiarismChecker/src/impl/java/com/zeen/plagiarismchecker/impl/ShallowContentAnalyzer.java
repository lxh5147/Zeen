package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.ContentAnalyzer;

public class ShallowContentAnalyzer implements ContentAnalyzer {

    public ShallowContentAnalyzer(Tokenizer tokenizer) {
        this.tokenizer = checkNotNull(tokenizer, "tokenizer");
    }

    private final Tokenizer tokenizer;

    @Override
    public Iterable<? extends CharSequence> getCheckPoints(String content) {
        // lower case and then tokenization
        Iterable<? extends CharSequence> originalCheckPoints = this.tokenizer
                .split(content.toLowerCase());

        assert (originalCheckPoints != null);
        List<CharSequence> checkPoints = Lists.newArrayList();
        originalCheckPoints.forEach(checkPoint -> {
            // remove stop word
                if (StopWordUtil.isStopWord(checkPoint)) {
                    return;
                }
                // stemming and stop word check
                CharSequence stemmedCheckPoint = STEMMER.get().stem(checkPoint);
                if (StopWordUtil.isStopWord(stemmedCheckPoint)) {
                    return;
                }
                checkPoints.add(stemmedCheckPoint);
            });
        return checkPoints;
    }

    // Stemmer is not thread safe
    private static final ThreadLocal<Stemmer> STEMMER = new ThreadLocal<Stemmer>() {
        @Override
        protected Stemmer initialValue() {
            return new SnowballStemmer(ALGORITHM.ENGLISH, 1);
        }
    };
}
