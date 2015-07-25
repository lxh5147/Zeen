package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

import com.zeen.plagiarismchecker.ContentAnalyzer;

public class SimpleContentAnalyzer implements ContentAnalyzer {

    public SimpleContentAnalyzer(Tokenizer tokenizer) {
        this.tokenizer = checkNotNull(tokenizer, "tokenizer");
    }

    @Override
    public Iterable<Iterable<CharSequence>> analyze(String content) {
        checkNotNull("content", "content");
        Iterable<CharSequence> checkPoints = Lists.newArrayList(this.tokenizer
                .split(content));
        List<Iterable<CharSequence>> checkPointsList = Lists.newArrayList();
        checkPointsList.add(checkPoints);
        return checkPointsList;
    }

    private final Tokenizer tokenizer;
}
