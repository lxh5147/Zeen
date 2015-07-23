package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.Fingerprint;
import com.zeen.plagiarismchecker.FingerprintRepository;
import com.zeen.plagiarismchecker.ParagraphEntry;

public class FingerprintRepositoryImpl implements FingerprintRepository {
    @Override
    public Iterable<ParagraphEntry> getFingerprintEntries(
            Fingerprint fingerprint) {
        checkNotNull(fingerprint, "fingerprint");
        long value = fingerprint.getValue();
        return this.index == null ? this
                .getFingerprintEntriesWithSortedValues(value) : this
                .getFingerprintEntriesWithSortedIndex(value);
    }

    private Iterable<ParagraphEntry> getFingerprintEntriesWithSortedValues(
            long value) {
        int pos = Arrays.binarySearch(this.values, 0, this.size, value);
        if (pos < 0) {
            return Collections.emptyList();
        }
        final List<ParagraphEntry> results = Lists.newArrayList();

        results.add(new ParagraphEntryImpl(this.articleEntries[pos],
                this.paragraphEntries[pos]));

        for (int i = pos + 1; i < this.size; ++i) {
            if (this.values[i] != value) {
                break;
            }
            results.add(new ParagraphEntryImpl(this.articleEntries[i],
                    this.paragraphEntries[i]));
        }
        for (int i = pos - 1; i >= 0; --i) {
            if (this.values[i] != value) {
                break;
            }
            results.add(new ParagraphEntryImpl(this.articleEntries[i],
                    this.paragraphEntries[i]));
        }
        return results;
    }

    private Iterable<ParagraphEntry> getFingerprintEntriesWithSortedIndex(
            long value) {

        assert this.index != null;

        int pos = new BinearySearchWithIndex(this.values, this.index, this.size)
                .search(value);

        if (pos < 0) {
            return Collections.emptyList();
        }
        final List<ParagraphEntry> results = Lists.newArrayList();

        int resolvedPos = this.index[pos];
        results.add(new ParagraphEntryImpl(this.articleEntries[resolvedPos],
                this.paragraphEntries[resolvedPos]));

        for (int i = pos + 1; i < this.size; ++i) {
            resolvedPos = this.index[i];
            if (this.values[resolvedPos] != value) {
                break;
            }
            results.add(new ParagraphEntryImpl(
                    this.articleEntries[resolvedPos],
                    this.paragraphEntries[resolvedPos]));
        }
        for (int i = pos - 1; i >= 0; --i) {
            resolvedPos = this.index[i];
            if (this.values[resolvedPos] != value) {
                break;
            }
            results.add(new ParagraphEntryImpl(
                    this.articleEntries[resolvedPos],
                    this.paragraphEntries[resolvedPos]));
        }
        return results;
    }

    static class BinearySearchWithIndex {
        private final int[] index;
        private final long[] values;
        private final int size;

        BinearySearchWithIndex(long[] values, int[] index, int size) {
            this.index = index;
            this.values = values;
            this.size = size;
        }

        final int search(long key) {
            return binarySearch0(0, this.size, key);
        }

        // Like public version, but without range checks.
        private final int binarySearch0(int fromIndex, int toIndex, long key) {
            int low = fromIndex;
            int high = toIndex - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                long midVal = this.values[this.index[mid]];
                if (midVal < key)
                    low = mid + 1;
                else if (midVal > key)
                    high = mid - 1;
                else
                    return mid; // key found
            }
            return -(low + 1); // key not found.
        }
    }

    public FingerprintRepositoryImpl(long[] values, int[] articleEntries,
            int[] paragraphEntries, int[] index, int size) {
        checkArgument(size > 0, "size");
        checkNotNull(values, "values");
        checkNotNull(articleEntries, "articleEntries");
        checkNotNull(paragraphEntries, "paragraphEntries");
        checkArgument(values.length >= size, "values");
        checkArgument(articleEntries.length >= size, "articleEntries");
        checkArgument(paragraphEntries.length >= size, "paragraphEntries");
        checkArgument(index == null || index.length >= size, "index");
        this.values = values;
        this.articleEntries = articleEntries;
        this.paragraphEntries = paragraphEntries;

        this.index = index;
        this.size = size;
        // index == null means the values are sorted; otherwise, index is the
        // sorted index, but values are not sorted

    }

    final long[] values;
    final int[] articleEntries;
    final int[] paragraphEntries;
    final int[] index;
    final int size;

    static class FingerprintImpl implements Fingerprint {
        FingerprintImpl(long value) {
            this.value = value;
        }

        private final long value;

        @Override
        public long getValue() {
            return value;
        }
    }

    public static Fingerprint newFingerprint(long value) {
        return new FingerprintImpl(value);
    }

    public static ParagraphEntry newParagraphEntry(int articleId,
            int paragraphId) {
        return new ParagraphEntryImpl(articleId, paragraphId);
    }

    static class ParagraphEntryImpl implements ParagraphEntry,
            Comparable<ParagraphEntry> {
        private final int articleId;
        private final int paragraphId;

        private ParagraphEntryImpl(int articleId, int paragraphId) {
            this.articleId = articleId;
            this.paragraphId = paragraphId;
        }

        @Override
        public int getArticleId() {
            return this.articleId;
        }

        @Override
        public int getParagraphId() {
            return this.paragraphId;
        }

        @Override
        public int compareTo(ParagraphEntry that) {
            return ComparisonChain.start()
                    .compare(this.getArticleId(), that.getArticleId())
                    .compare(this.getParagraphId(), that.getParagraphId())
                    .result();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("articleId", this.articleId)
                    .add("paragraphId", this.paragraphId).toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.articleId, this.paragraphId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParagraphEntry other = (ParagraphEntry) obj;

            return Objects.equal(this.getArticleId(), other.getArticleId())
                    && Objects.equal(this.getParagraphId(),
                            other.getParagraphId());
        }
    }

    // save to disk
    public void save(File file) throws IOException {
        checkNotNull(file, "file");

        try (ObjectOutputStream writer = new ObjectOutputStream(
                new BufferedOutputStream(new GZIPOutputStream(
                        new FileOutputStream(file))))) {
            writer.writeInt(this.size);
            if (this.index == null) {
                for (int i = 0; i < this.size; ++i) {
                    writer.writeLong(this.values[i]);
                    writer.writeInt(this.articleEntries[i]);
                    writer.writeInt(this.paragraphEntries[i]);
                }
            } else {
                for (int i = 0; i < this.size; ++i) {
                    writer.writeLong(this.values[this.index[i]]);
                    writer.writeInt(this.articleEntries[this.index[i]]);
                    writer.writeInt(this.paragraphEntries[this.index[i]]);
                }
            }
        }
    }

    public static FingerprintRepository load(File file)
            throws FileNotFoundException, IOException {
        checkNotNull(file, "file");
        try (ObjectInputStream reader = new ObjectInputStream(
                new BufferedInputStream(new GZIPInputStream(
                        new FileInputStream(file))))) {
            int size = reader.readInt();
            assert (size >= 0);
            long[] values = new long[size];
            int[] articleEntries = new int[size];
            int[] paragraphEntries = new int[size];
            for (int i = 0; i < size; ++i) {
                values[i] = reader.readLong();
                articleEntries[i] = reader.readInt();
                paragraphEntries[i] = reader.readInt();
            }
            return new FingerprintRepositoryImpl(values, articleEntries,
                    paragraphEntries, null, size);
        }
    }
}
