package com.zeen.plagiarismchecker.impl;

public interface SegmentSplitter {
    Iterable<? extends CharSequence> split(CharSequence content);
}
