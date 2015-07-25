package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.zeen.plagiarismchecker.ContentAnalyzer;

public class SimpleContentAnalyzer implements ContentAnalyzer {

    public SimpleContentAnalyzer(Tokenizer tokenizer) {
        this.tokenizer = checkNotNull(tokenizer, "tokenizer");
    }

    @Override
    public Iterable<? extends CharSequence> analyze(String content) {
        checkNotNull("content", "content");
        return this.tokenizer.split(content);
    }

    private final Tokenizer tokenizer;
}
