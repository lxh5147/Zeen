package com.zeen.plagiarismchecker.impl;

import com.zeen.plagiarismchecker.ContentAnalizer;

public enum ContentAnalizers {
    SimpleContentAnalizerWithSimpleTokenizer {
	@Override
	public
	ContentAnalizer getContentAnalizer() {
	    return SIMPLE_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIZER;
	}
    },
    SimpleContentAnalizerWithOpenNLPTokenizer {
	@Override
	public
	ContentAnalizer getContentAnalizer() {
	    return SIMPLE_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIZER;
	}
    },
    ShallowContentAnalizerWithSimpleTokenizer {
	@Override
	public
	ContentAnalizer getContentAnalizer() {
	    return SHALLOW_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER;
	}
    },
    ShallowContentAnalizerWithOpenNLPTokenizer {
	@Override
	public
	ContentAnalizer getContentAnalizer() {
	    return SHALLOW_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER;
	}
    },
    BagOfWordsContentAnalizerWithSimpleTokenizer {
	@Override
	public
	ContentAnalizer getContentAnalizer() {
	    return BAGOFWORDS_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER;
	}
    },
    BagOfWordsContentAnalizerWithOpenNLPTokenizer {
	@Override
	public
	ContentAnalizer getContentAnalizer() {
	    return BAGOFWORDS_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER;
	}
    };
    public abstract ContentAnalizer getContentAnalizer();

    private static final Tokenizer SIMPLE_TOKENIZER = new SimpleTokenizer();
    private static final Tokenizer OPENNLP_TOKENIZER = new OpenNLPTokenizer();

    private static final ContentAnalizer SIMPLE_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIZER = new SimpleContentAnalizer(
	    SIMPLE_TOKENIZER);
    private static final ContentAnalizer SIMPLE_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIZER = new SimpleContentAnalizer(
	    OPENNLP_TOKENIZER);
    private static final ContentAnalizer SHALLOW_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER = new ShallowContentAnalizer(
	    SIMPLE_TOKENIZER);
    private static final ContentAnalizer SHALLOW_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER = new ShallowContentAnalizer(
	    OPENNLP_TOKENIZER);
    private static final ContentAnalizer BAGOFWORDS_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER = new BagOfWordsContentAnalizer(
	    SHALLOW_CONTENT_ANALIZER_WITH_SIMPLE_TOKENIER);
    private static final ContentAnalizer BAGOFWORDS_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER = new BagOfWordsContentAnalizer(
	    SHALLOW_CONTENT_ANALIZER_WITH_OPENNLP_TOKENIER);
}
