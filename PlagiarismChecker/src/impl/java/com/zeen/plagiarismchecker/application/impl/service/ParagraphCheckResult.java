package com.zeen.plagiarismchecker.application.impl.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

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
    
    private static final ThreadLocal<JSONParser> JSON_PARSER = new ThreadLocal<JSONParser>() {
        @Override
        protected JSONParser initialValue() {
            return new JSONParser();
        }
    };
    
    @SuppressWarnings("unchecked")
    public static List<ParagraphCheckResult> getParagraphCheckResult(
            final String jsonResponse) throws ParseException {
        checkNotNull(jsonResponse, "jsonResponse");        
        List<ParagraphCheckResult> results = Lists.newArrayList();      
        JSONArray jsonArray = (JSONArray) JSON_PARSER.get().parse(jsonResponse);
        jsonArray.forEach(jsonObject -> {
            results.add(getParagraphCheckResult((JSONObject) jsonObject));
        });
        return results;
    }

    private static ParagraphCheckResult getParagraphCheckResult(JSONObject jsonObject) {
        ParagraphCheckResult paragraphCheckResult = new ParagraphCheckResult();
        paragraphCheckResult.setParagraphContentToCheck((String) jsonObject
                .get("paragraphContentToCheck"));
        paragraphCheckResult
                .setCheckResults(getCheckResults((JSONArray) jsonObject
                        .get("checkResults")));
        return paragraphCheckResult;
    }
    
    @SuppressWarnings("unchecked")
    private static List<CheckResult> getCheckResults(JSONArray jsonArray) {
        List<CheckResult> results = Lists.newArrayList();
        jsonArray.forEach(jsonObject -> {
            results.add(CheckResult.getCheckResult((JSONObject) jsonObject));
        });
        return results;
    }
}

