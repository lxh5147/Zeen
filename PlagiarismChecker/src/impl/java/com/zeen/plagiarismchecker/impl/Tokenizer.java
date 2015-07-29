package com.zeen.plagiarismchecker.impl;

public interface Tokenizer {
    Iterable<? extends CharSequence> split(CharSequence content);
}
