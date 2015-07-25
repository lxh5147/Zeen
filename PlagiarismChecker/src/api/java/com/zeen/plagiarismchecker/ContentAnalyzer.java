package com.zeen.plagiarismchecker;

public interface ContentAnalyzer {
    Iterable<CharSequence> analyze(final String content);
}
