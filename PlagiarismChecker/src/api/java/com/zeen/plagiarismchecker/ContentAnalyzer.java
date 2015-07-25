package com.zeen.plagiarismchecker;

public interface ContentAnalyzer {
    Iterable<? extends CharSequence> analyze(final String content);
}
