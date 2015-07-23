package com.zeen.plagiarismchecker.impl;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

public class FingerprintRepositoryImplTest {
	@Test
	public void withSortedValueTest() {
		Assert.assertEquals(
				Sets.newHashSet( FingerprintRepositoryImpl.newParagraphEntry(
						1, 5)),
				Sets.newHashSet(new FingerprintRepositoryImpl(new long[] { 1,
						2, 3 }, new int[] { 2, 1, 3 }, new int[] { 4, 5, 6 },
						null, 3)
						.getFingerprintEntries(new FingerprintRepositoryImpl.FingerprintImpl(
								2))));

		Assert.assertEquals(
				Sets.newHashSet(
				        FingerprintRepositoryImpl.newParagraphEntry(1, 5),
				        FingerprintRepositoryImpl.newParagraphEntry(4, 7),
				        FingerprintRepositoryImpl.newParagraphEntry(7, 8)),
				Sets.newHashSet(new FingerprintRepositoryImpl(new long[] { 1,
						2, 2, 2, 3 }, new int[] { 2, 1, 4, 7, 3 }, new int[] {
						4, 5, 7, 8, 6 }, null, 5)
						.getFingerprintEntries(new FingerprintRepositoryImpl.FingerprintImpl(
								2))));
	}

	@Test
	public void withSortedIndexTest() {
		Assert.assertEquals(
				Sets.newHashSet(FingerprintRepositoryImpl.newParagraphEntry(
						3, 6)),
				Sets.newHashSet(new FingerprintRepositoryImpl(new long[] { 3,
						1, 2 }, new int[] { 2, 1, 3 }, new int[] { 4, 5, 6 },
						new int[] { 1, 2, 0 }, 3)
						.getFingerprintEntries(new FingerprintRepositoryImpl.FingerprintImpl(
								2))));

		Assert.assertEquals(
				Sets.newHashSet(
				        FingerprintRepositoryImpl.newParagraphEntry(1, 5),
				        FingerprintRepositoryImpl.newParagraphEntry(7, 8),
				        FingerprintRepositoryImpl.newParagraphEntry(3, 6)),
				Sets.newHashSet(new FingerprintRepositoryImpl(new long[] { 3,
						2, 1, 2, 2 }, new int[] { 2, 1, 4, 7, 3 }, new int[] {
						4, 5, 7, 8, 6 }, new int[] { 2, 1, 3, 4, 0 }, 5)
						.getFingerprintEntries(new FingerprintRepositoryImpl.FingerprintImpl(
								2))));
	}

	@Test
	public void saveLoadWithoutTest() throws IOException {
		new FingerprintRepositoryImpl(new long[] { 1, 2, 3 }, new int[] { 2, 1,
				3 }, new int[] { 4, 5, 6 }, null, 3).save(new File("test"));
		FingerprintRepositoryImpl loaded = (FingerprintRepositoryImpl) FingerprintRepositoryImpl
				.load(new File("test"));
		Assert.assertNotNull(loaded);
		Assert.assertArrayEquals(new long[] { 1, 2, 3 }, loaded.values);
		Assert.assertArrayEquals(new int[] { 2, 1, 3 }, loaded.articleEntries);
		Assert.assertArrayEquals(new int[] { 4, 5, 6 }, loaded.paragraphEntries);
		Assert.assertNull(loaded.index);
		Assert.assertEquals(3, loaded.size);
		new File("test").delete();
	}

	@Test
	public void saveLoadWithIndexTest() throws IOException {
		new FingerprintRepositoryImpl(new long[] { 3, 2, 1, 2, 2 }, new int[] {
				2, 1, 4, 7, 3 }, new int[] { 4, 5, 7, 8, 6 }, new int[] { 2, 1,
				3, 4, 0 }, 5).save(new File("test"));
		FingerprintRepositoryImpl loaded = (FingerprintRepositoryImpl) FingerprintRepositoryImpl
				.load(new File("test"));
		Assert.assertNotNull(loaded);
		Assert.assertArrayEquals(new long[] { 1, 2, 2, 2, 3 }, loaded.values);
		Assert.assertArrayEquals(new int[] { 4, 1, 7, 3, 2 },
				loaded.articleEntries);
		Assert.assertArrayEquals(new int[] { 7, 5, 8, 6, 4 },
				loaded.paragraphEntries);
		Assert.assertNull(loaded.index);
		Assert.assertEquals(5, loaded.size);
		new File("test").delete();
	}
}
