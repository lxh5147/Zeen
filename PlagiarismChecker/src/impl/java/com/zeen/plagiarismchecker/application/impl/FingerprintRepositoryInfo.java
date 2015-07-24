package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
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

    public ContentAnalyzerType getContentAnalyzerType() {
        return this.contentAnalyzerType;
    }

    public File getIndexFile() {
        return this.indexFile;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("contentAnalyzerType", this.contentAnalyzerType)
                .add("indexFile", this.indexFile).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.contentAnalyzerType, this.indexFile);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FingerprintRepositoryInfo other = (FingerprintRepositoryInfo) obj;
        return Objects.equal(this.getContentAnalyzerType(),
                other.getContentAnalyzerType())
                && Objects.equal(this.getIndexFile(), other.getIndexFile());
    }
}
