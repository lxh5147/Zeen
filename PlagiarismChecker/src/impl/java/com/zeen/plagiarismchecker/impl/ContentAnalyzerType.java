package com.zeen.plagiarismchecker.impl;

import com.zeen.plagiarismchecker.ContentAnalyzer;

public enum ContentAnalyzerType {
    SimpleContentAnalizerWithSimpleTokenizer {
        @Override
        public ContentAnalyzer getContentAnalyzer() {
            return SIMPLE_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIZER;
        }
    },
    SimpleContentAnalizerWithOpenNLPTokenizer {
        @Override
        public ContentAnalyzer getContentAnalyzer() {
            return SIMPLE_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIZER;
        }
    },
    ShallowContentAnalizerWithSimpleTokenizer {
        @Override
        public ContentAnalyzer getContentAnalyzer() {
            return SHALLOW_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER;
        }
    },
    ShallowContentAnalizerWithOpenNLPTokenizer {
        @Override
        public ContentAnalyzer getContentAnalyzer() {
            return SHALLOW_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER;
        }
    },
    BagOfWordsContentAnalizerWithSimpleTokenizer {
        @Override
        public ContentAnalyzer getContentAnalyzer() {
            return BAGOFWORDS_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER;
        }
    },
    BagOfWordsContentAnalizerWithOpenNLPTokenizer {
        @Override
        public ContentAnalyzer getContentAnalyzer() {
            return BAGOFWORDS_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER;
        }
    },
    SegmentContentAnalizerWithSimpleSegmentSplitter {
        @Override
        public ContentAnalyzer getContentAnalyzer() {
            return SEGMENT_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIZER_AND_SIMPLE_SEGMENT_SPLITTER;
        }
    };
    public abstract ContentAnalyzer getContentAnalyzer();

    private static final Tokenizer SIMPLE_TOKENIZER = new SimpleTokenizer();
    private static final Tokenizer OPENNLP_TOKENIZER = new OpenNLPTokenizer();
    private static final SegmentSplitter SIMPLE_SEGMENT_SPLITTER = new SimpleSegmentSplitter();

    private static final ContentAnalyzer SIMPLE_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIZER = new SimpleContentAnalyzer(
            SIMPLE_TOKENIZER);
    private static final ContentAnalyzer SIMPLE_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIZER = new SimpleContentAnalyzer(
            OPENNLP_TOKENIZER);
    private static final ContentAnalyzer SHALLOW_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER = new ShallowContentAnalyzer(
            SIMPLE_TOKENIZER);
    private static final ContentAnalyzer SHALLOW_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER = new ShallowContentAnalyzer(
            OPENNLP_TOKENIZER);
    private static final ContentAnalyzer BAGOFWORDS_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER = new BagOfWordsContentAnalyzer(
            SHALLOW_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER);
    private static final ContentAnalyzer BAGOFWORDS_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER = new BagOfWordsContentAnalyzer(
            SHALLOW_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER);

    private static final int MINIMAL_TOKENS_PER_SEGMENT = 20;
    private static final int MAXIMAL_TOKENS_PER_SEGMENT = 40;

    private static final ContentAnalyzer SEGMENT_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIZER_AND_SIMPLE_SEGMENT_SPLITTER = new ContentSegmentAnalyzer(
            SIMPLE_TOKENIZER, SIMPLE_SEGMENT_SPLITTER,
            MINIMAL_TOKENS_PER_SEGMENT, MAXIMAL_TOKENS_PER_SEGMENT);

}
