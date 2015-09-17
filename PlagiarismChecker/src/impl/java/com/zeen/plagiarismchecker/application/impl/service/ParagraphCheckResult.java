package com.zeen.plagiarismchecker.application.impl.service;

import java.util.List;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;


public  class ParagraphCheckResult {
    private String paragraphContentToCheck;
    private List<CheckResult> checkResults;

    public ParagraphCheckResult() {
    }

    public String getParagraphContentToCheck() {
        return this.paragraphContentToCheck;
    }

    public List<CheckResult> getCheckResults() {
        return this.checkResults;
    }

    public void setParagraphContentToCheck(String paragraphContentToCheck) {
        this.paragraphContentToCheck = paragraphContentToCheck;
    }

    public void setCheckResults(List<CheckResult> checkResults) {
        this.checkResults = checkResults;
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this.getClass())
                .add("paragraphContentToCheck",
                        this.paragraphContentToCheck)
                .add("checkResults", this.checkResults).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.paragraphContentToCheck,
                this.checkResults);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParagraphCheckResult other = (ParagraphCheckResult) obj;
        return Objects.equal(this.getParagraphContentToCheck(),
                other.getParagraphContentToCheck())
                && Objects.equal(this.getCheckResults(),
                        other.getCheckResults());
    }
}

