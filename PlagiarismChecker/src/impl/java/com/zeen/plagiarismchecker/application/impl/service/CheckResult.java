package com.zeen.plagiarismchecker.application.impl.service;

import java.util.List;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.zeen.plagiarismchecker.impl.ContentAnalyzerType;

public class CheckResult {
    private int articleId;
    private int paragraphId;
    private String paragraphContent;
    private List<ContentAnalyzerType> hittedContentAnalizerTypes;

    // to make it a bean
    public CheckResult() {
    }

    CheckResult(int articleId, int paragraphId, String paragraphContent,
            List<ContentAnalyzerType> hittedContentAnalizerTypes) {
        this.articleId = articleId;
        this.paragraphId = paragraphId;
        this.paragraphContent = paragraphContent;
        this.hittedContentAnalizerTypes = hittedContentAnalizerTypes;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this.getClass())
                .add("articleId", this.articleId)
                .add("paragraphId", this.paragraphId)
                .add("paragraphContent", this.paragraphContent)
                .add("hittedContentAnalizerTypes",
                        this.hittedContentAnalizerTypes).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.articleId, this.paragraphId,
                this.paragraphContent, this.hittedContentAnalizerTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CheckResult other = (CheckResult) obj;
        return Objects.equal(this.getArticleId(), other.getArticleId())
                && Objects.equal(this.getParagraphId(), other.getParagraphId())
                && Objects.equal(this.getParagraphContent(),
                        other.getParagraphContent())
                && Objects.equal(this.getHittedContentAnalizerTypes(),
                        other.getHittedContentAnalizerTypes());
    }

    public int getArticleId() {
        return this.articleId;
    }

    public int getParagraphId() {
        return this.paragraphId;
    }

    public String getParagraphContent() {
        return this.paragraphContent;
    }

    public List<ContentAnalyzerType> getHittedContentAnalizerTypes() {
        return this.hittedContentAnalizerTypes;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public void setParagraphId(int paragraphId) {
        this.paragraphId = paragraphId;
    }

    public void setParagraphContent(String paragraphContent) {
        this.paragraphContent = paragraphContent;
    }

    public void setHittedContentAnalizerTypes(
            List<ContentAnalyzerType> hittedContentAnalizerTypes) {
        this.hittedContentAnalizerTypes = hittedContentAnalizerTypes;
    }
}
