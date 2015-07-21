package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.TreeMap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zeen.plagiarismchecker.ContentAnalizer;

public class BagOfWordsContentAnalizer implements ContentAnalizer {

	public BagOfWordsContentAnalizer(ContentAnalizer contentAnalizer) {
		this.contentAnalizer = checkNotNull(contentAnalizer, "contentAnalizer");
	}

	private final ContentAnalizer contentAnalizer;

	@Override
	public Iterable<? extends CharSequence> getCheckPoints(String content) {
		Iterable<? extends CharSequence> originalCheckPoints = this.contentAnalizer
				.getCheckPoints(content);
		assert (originalCheckPoints != null);
		TreeMap<String, Integer> bagOfWords = Maps.newTreeMap();
		originalCheckPoints.forEach(checkPoint -> {
			Integer count = bagOfWords.get(checkPoint);
			count = count != null ? count + 1 : Integer.valueOf(1);
			bagOfWords.put(new StringBuilder().append(checkPoint).toString(),
					count);
		});
		List<CharSequence> checkPoints = Lists.newArrayList();
		bagOfWords.entrySet().forEach(
				entry -> {
					checkPoints.add(Joiner.on(":").join(entry.getKey(),
							entry.getValue()));
				});
		return checkPoints;
	}

}
