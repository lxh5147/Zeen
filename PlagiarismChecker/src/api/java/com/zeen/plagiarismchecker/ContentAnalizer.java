package com.zeen.plagiarismchecker;

public interface ContentAnalizer {
	Iterable<? extends CharSequence> getCheckPoints(final String content);
}
