package com.zeen.plagiarismchecker.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.Article;

public class ArticleRepositoryImplTest {

	static final String[][] ARTICLES = new String[][] {
			{
					"this work uses deep learning to represent a reference. we can summurize our contributions as follows.",
					"deep learning has attracted increasing interestes in recent studies on nlp. it has been widely applied to word embedding, language modeling." },
			{ "it is still not clear if and how deep learning can help to detect repeated text. In this work, we design a mapping from a paragraph to a paragraph vector, which is used to detect copy&past text." },
			{
					"experimental results show our method outperforms the method based on bag of words.",
					"in future we will apply our method to other languages, such as chinese." } };

	@Before
	public void setup() throws IOException {
		// create two folders
		new File("ref1").mkdirs();
		this.write(new File("ref1/0"), ARTICLES[0]);
		this.write(new File("ref1/1"), ARTICLES[1]);
		new File("ref2").mkdirs();
		this.write(new File("ref2/2"), ARTICLES[2]);
	}

	private void write(File file, String[] lines)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"))) {
			for (int i = 0; i < lines.length; ++i) {
				writer.write(lines[i]);
				writer.newLine();
			}
		}
	}

	@After
	public void tearDown() {
		new File("ref1/0").delete();
		new File("ref1/1").delete();
		new File("ref1").delete();
		new File("ref2/2").delete();
		new File("ref2").delete();
	}

	@Test
	public void readReferencesTest() {
		ArticleRepositoryImpl referenceRepository = new ArticleRepositoryImpl(
				Lists.newArrayList(Paths.get("ref1"), Paths.get("ref2")));
		// load every reference into memory
		List<Article> references = Lists.newArrayList(referenceRepository
				.getArticles());
		Assert.assertEquals(ARTICLES.length, references.size());
		for (int i = 0; i < references.size(); ++i) {
			Article reference = references.get(i);
			Assert.assertEquals(i, reference.getId());
			List<String> paragraphes = Lists.newArrayList();
			reference.getParagraphes().forEach(paragraph -> {
				paragraphes.add(paragraph.getContent());
			});
			Assert.assertEquals(Lists.newArrayList(ARTICLES[i]), paragraphes);
		}
	}

	@Test
	public void getReferenceTest() {
		ArticleRepositoryImpl referenceRepository = new ArticleRepositoryImpl(
				Lists.newArrayList(Paths.get("ref1"), Paths.get("ref2")));
		for (int i = 0; i < ARTICLES.length; ++i) {
			Article reference = referenceRepository.getArticle(i);
			Assert.assertEquals(i, reference.getId());
			List<String> paragraphes = Lists.newArrayList();
			reference.getParagraphes().forEach(paragraph -> {
				paragraphes.add(paragraph.getContent());
			});
			Assert.assertEquals(Lists.newArrayList(ARTICLES[i]), paragraphes);
		}
	}

}
