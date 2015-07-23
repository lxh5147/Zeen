package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import com.zeen.plagiarismchecker.ContentAnalyzer;

public class FingerprintRepositoryInfo {
    final ContentAnalyzer contentAnalyzer;
    final File indexFile;

    public FingerprintRepositoryInfo(final ContentAnalyzer contentAnalizer,
            final File indexFile) {
        checkNotNull(contentAnalizer, "contentAnalizer");
        checkNotNull(indexFile, "indexFile");
        this.contentAnalyzer = contentAnalizer;
        this.indexFile = indexFile;
    }
}
