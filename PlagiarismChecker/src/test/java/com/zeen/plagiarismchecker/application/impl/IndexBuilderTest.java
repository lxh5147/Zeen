package com.zeen.plagiarismchecker.application.impl;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.zeen.plagiarismchecker.application.impl.IndexBuilder;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryTestUtil;
import com.zeen.plagiarismchecker.impl.ContentAnalizers;

public class IndexBuilderTest {

	@Before
	public void setup() throws IOException {
		ArticleRepositoryTestUtil.setupArticleRepository();
	}

	@After
	public void tearDown() {
		ArticleRepositoryTestUtil.tearDownArticleRepository();
	}

	@Test
	public void getIndexBuilderTest() throws ParseException, IOException {
		String[] args = {
				"--articleRepositoryFolders",
				Joiner.on(',').join(ArticleRepositoryTestUtil.FOLDERS),
				"--contentAnalizers",
				Joiner.on(',')
						.join(ContentAnalizers.SimpleContentAnalizerWithSimpleTokenizer
								.name(),
								ContentAnalizers.BagOfWordsContentAnalizerWithOpenNLPTokenizer
										.name()), "--indexPath", "",
				"--capability", String.valueOf(10000) };
		IndexBuilder indexBuilder = IndexBuilder.getIndexBuilderWithArgs(args);
		Assert.assertNotNull(indexBuilder);
	}
}
