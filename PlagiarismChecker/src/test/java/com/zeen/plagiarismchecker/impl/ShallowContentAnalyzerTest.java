package com.zeen.plagiarismchecker.impl;


import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.ContentAnalyzer;

public class ShallowContentAnalyzerTest {
	@Test
	public void getCheckPointsTest() {
		ContentAnalyzer contentAnalizer = new ShallowContentAnalyzer( new SimpleTokenizer());
		//lower case and stop words removal
		Assert.assertEquals(Lists.newArrayList("s"), Lists
				.newArrayList(contentAnalizer.getCheckPoints("This'S me.")));
		//numbers
		Assert.assertEquals(Lists.newArrayList("number", "123"), Lists
				.newArrayList(contentAnalizer.getCheckPoints("number 123?")));
		//stemming
		Assert.assertEquals(Lists.newArrayList("good","test"), Lists
				.newArrayList(contentAnalizer.getCheckPoints("good tests!")));
	}
}
