package com.zeen.plagiarismchecker;

public interface ContentAnalyzer {
    Iterable<? extends CharSequence> getCheckPoints(final String content);
}
