package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import com.zeen.plagiarismchecker.ContentAnalizer;

class FingerprintRepositoryInfo {
    final ContentAnalizer contentAnalizer;
    final File indexFile;

    FingerprintRepositoryInfo(final ContentAnalizer contentAnalizer,
            final File indexFile) {
        checkNotNull(contentAnalizer, "contentAnalizer");
        checkNotNull(indexFile, "indexFile");
        this.contentAnalizer = contentAnalizer;
        this.indexFile = indexFile;
    }
}
