package com.zeen.plagiarismchecker.impl;



import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.ContentAnalyzer;

public class SimpleContentAnalyzerTest {
	@Test
	public void getCheckPointsTest() {
		ContentAnalyzer contentAnalizer = new SimpleContentAnalyzer(new SimpleTokenizer());
		//keep case
		Assert.assertEquals(Lists.newArrayList("This", "s", "me"), Lists
				.newArrayList(contentAnalizer.getCheckPoints("This's me.")));
		//numbers
		Assert.assertEquals(Lists.newArrayList("number", "123", "yes"), Lists
				.newArrayList(contentAnalizer.getCheckPoints("number 123? yes!")));
	}
}
