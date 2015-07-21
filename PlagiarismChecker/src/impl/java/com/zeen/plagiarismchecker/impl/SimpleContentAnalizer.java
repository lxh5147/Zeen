package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.zeen.plagiarismchecker.ContentAnalizer;

public class SimpleContentAnalizer implements ContentAnalizer {

	public SimpleContentAnalizer(Tokenizer tokenizer) {
		this.tokenizer = checkNotNull(tokenizer, "tokenizer");
	}

	@Override
	public Iterable<? extends CharSequence> getCheckPoints(String content) {
		checkNotNull("content", "content");
		return this.tokenizer.split(content);
	}

	private final Tokenizer tokenizer;
}
