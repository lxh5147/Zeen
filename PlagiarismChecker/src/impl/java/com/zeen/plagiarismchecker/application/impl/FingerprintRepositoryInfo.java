package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import com.zeen.plagiarismchecker.impl.ContentAnalyzerType;

public class FingerprintRepositoryInfo {
    private final ContentAnalyzerType contentAnalyzerType;
    private final File indexFile;

    public FingerprintRepositoryInfo(
            final ContentAnalyzerType contentAnalizerType, final File indexFile) {
        checkNotNull(contentAnalizerType, "contentAnalizerType");
        checkNotNull(indexFile, "indexFile");
        this.contentAnalyzerType = contentAnalizerType;
        this.indexFile = indexFile;
    }
    public ContentAnalyzerType getContentAnalyzerType(){
        return this.contentAnalyzerType;
    }
    public File getIndexFile(){
        return this.indexFile;
    }
}
