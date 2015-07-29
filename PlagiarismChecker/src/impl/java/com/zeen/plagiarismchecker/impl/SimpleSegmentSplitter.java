package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public class SimpleSegmentSplitter implements SegmentSplitter {

    private static final Splitter SPLITTER = Splitter
            .on(CharMatcher.is('.').or(CharMatcher.is('!'))
                    .or(CharMatcher.is('?')).precomputed()).trimResults()
            .omitEmptyStrings();

    @Override
    public Iterable<? extends CharSequence> split(CharSequence content) {
        checkNotNull(content, "content");
        return SPLITTER.split(content);
    }
}
