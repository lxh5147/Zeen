package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

import com.zeen.plagiarismchecker.ContentAnalyzer;

//This content analyzer generates segments, and then generates a check point list for each segment
public class ContentSegmentAnalyzer implements ContentAnalyzer {

    public ContentSegmentAnalyzer(Tokenizer tokenizer,
            SegmentSplitter segmentSplitter,
            int minimalRequiredTokensPerSegment,
            int maximalAllowedTokensPerSegment) {
        checkNotNull(tokenizer, "tokenizer");
        checkNotNull(segmentSplitter, "segmentSplitter");
        checkArgument(minimalRequiredTokensPerSegment > 0,
                "minimalRequiredTokensPerSegment");
        checkArgument(
                maximalAllowedTokensPerSegment >= minimalRequiredTokensPerSegment,
                "maximalAllowedTokensPerSegment");
        this.tokenizer = tokenizer;
        this.segmentSplitter = segmentSplitter;
        this.minimalRequiredTokensPerSegment = minimalRequiredTokensPerSegment;
        this.maximalAllowedTokensPerSegment = maximalAllowedTokensPerSegment;
    }

    private final SegmentSplitter segmentSplitter;
    private final Tokenizer tokenizer;
    private final int minimalRequiredTokensPerSegment;
    private final int maximalAllowedTokensPerSegment;

    @Override
    public Iterable<Iterable<CharSequence>> analyze(String content) {
        checkNotNull("content", "content");
        List<CharSequence> segments = Lists.newArrayList(this.segmentSplitter
                .split(content));
        List<List<CharSequence>> segmentTokensList = Lists
                .newArrayListWithCapacity(segments.size());
        for (CharSequence segment : segments) {
            segmentTokensList.add(Lists.newArrayList(this.tokenizer
                    .split(segment)));
        }
        List<Iterable<CharSequence>> checkPointsList = Lists.newArrayList();
        for (int i = 0; i < segments.size(); ++i) {
            List<CharSequence> checkPoints = this.getCheckPoint(
                    segmentTokensList, i);
            if (!checkPoints.isEmpty()) {
                checkPointsList.add(checkPoints);
            }
        }
        return checkPointsList;
    }

    private List<CharSequence> getCheckPoint(
            List<List<CharSequence>> segmentTokensList, int i) {
        List<CharSequence> segmentTokens = segmentTokensList.get(i);
        List<CharSequence> checkPoints = Lists
                .newArrayListWithCapacity(this.maximalAllowedTokensPerSegment);
        for (CharSequence token : segmentTokens) {
            checkPoints.add(token);
        }
        if (checkPoints.size() >= this.minimalRequiredTokensPerSegment) {
            return checkPoints;
        }
        int indexOfSegmentToBorrow = i + 1;
        while (indexOfSegmentToBorrow < segmentTokensList.size()) {
            segmentTokens = segmentTokensList.get(indexOfSegmentToBorrow);
            for (CharSequence token : segmentTokens) {
                checkPoints.add(token);
                if (checkPoints.size() > this.maximalAllowedTokensPerSegment) {
                    return checkPoints;
                }
            }
            ++indexOfSegmentToBorrow;
        }
        return checkPoints;
    }

}
