package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public class SimpleTokenizer implements Tokenizer {

	private static final Splitter SPLITTER = Splitter
			.on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).trimResults()
			.omitEmptyStrings();

	@Override
	public Iterable<? extends CharSequence> split(String content) {
		checkNotNull(content, "content");
		return SPLITTER.split(content);
	}

}
