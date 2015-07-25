package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.stream.IntStream;

import jersey.repackaged.com.google.common.collect.Lists;

import com.zeen.plagiarismchecker.ContentAnalyzer;
import com.zeen.plagiarismchecker.FingerprintRepository;
import com.zeen.plagiarismchecker.FingerprintRepositoryBuilder;
import com.zeen.plagiarismchecker.Paragraph;

public class FingerprintRepositoryBuilderImpl implements
        FingerprintRepositoryBuilder {

    long[] values;
    int[] articleEntries;
    int[] paragraphEntries;
    int size;
    ContentAnalyzer analyzer;
    StringBuilder stringBuilder;
    long[] fingerprintBuffer;

    @Override
    public void start(ContentAnalyzer analyzer, int capability) {
        checkNotNull(analyzer, "analyzer");
        checkArgument(capability > 0, "capability");
        this.analyzer = analyzer;
        this.size = 0;
        this.stringBuilder = new StringBuilder();
        this.values = new long[capability];
        this.articleEntries = new int[capability];
        this.paragraphEntries = new int[capability];
        this.fingerprintBuffer = new long[ContentAnalyzer.MAX_LENGTH_OF_CHECKPOINTS_LIST_PER_ANALYZER];
    }

    @Override
    public void add(Paragraph paragraph) {
        checkNotNull(paragraph, "paragraph");
        checkState(this.analyzer != null, "analyzer");
        checkState(this.size < this.values.length, "size");
        String content = paragraph.getContent();
        List<Iterable<CharSequence>> checkPointsList = Lists
                .newArrayList(this.analyzer.analyze(content));
        FINGERPRINT_BUILDER.buildFingerprints(checkPointsList,
                this.stringBuilder, this.fingerprintBuffer);
        for (int i = 0; i < checkPointsList.size(); ++i) {
            this.values[this.size] = this.fingerprintBuffer[i];
            this.articleEntries[this.size] = paragraph.getArticleId();
            this.paragraphEntries[this.size] = paragraph.getId();
            ++this.size;
        }
    }

    @Override
    public void add(Iterable<Paragraph> paragraphs, int parallelism) {
        checkNotNull(paragraphs, "paragraphs");
        checkArgument(parallelism >= 1, "parallelism");
        checkState(this.analyzer != null, "analyzer");

        final List<List<Long>> valuesList = Lists.newArrayList();
        final List<List<Integer>> articleEntryiesList = Lists.newArrayList();
        final List<List<Integer>> paragraphEntriesList = Lists.newArrayList();
        for (int i = 0; i < parallelism; ++i) {
            valuesList.add(null);
            articleEntryiesList.add(null);
            paragraphEntriesList.add(null);
        }
        IntStream
                .range(0, parallelism)
                .parallel()
                .forEach(
                        i -> {
                            long[] curFingerprintBuffer = new long[ContentAnalyzer.MAX_LENGTH_OF_CHECKPOINTS_LIST_PER_ANALYZER];
                            StringBuilder curStringBuilder = new StringBuilder();
                            List<Long> curValues = Lists.newArrayList();
                            List<Integer> curArticleEntries = Lists
                                    .newArrayList();
                            List<Integer> curParagraphEntries = Lists
                                    .newArrayList();
                            paragraphs.forEach(paragraph -> {
                                String content = paragraph.getContent();
                                List<Iterable<CharSequence>> checkPointsList = Lists
                                        .newArrayList(this.analyzer
                                                .analyze(content));
                                FINGERPRINT_BUILDER.buildFingerprints(
                                        checkPointsList, curStringBuilder,
                                        curFingerprintBuffer);
                                for (int j = 0; j < checkPointsList.size(); ++j) {
                                    curValues.add(curFingerprintBuffer[j]);
                                    curArticleEntries.add(paragraph
                                            .getArticleId());
                                    curParagraphEntries.add(paragraph.getId());
                                }
                            });
                            valuesList.set(i, curValues);
                            articleEntryiesList.set(i, curArticleEntries);
                            paragraphEntriesList.set(i, curParagraphEntries);
                        });

        // now check the size
        int total = 0;
        for (int i = 0; i < parallelism; ++i) {
            total += valuesList.get(i).size();
        }
        checkState(this.size + total < this.values.length, "size");

        // now merge
        for (int i = 0; i < parallelism; ++i) {
            List<Long> curValues = valuesList.get(i);
            List<Integer> curArticleEntries = articleEntryiesList.get(i);
            List<Integer> curParagraphEntries = paragraphEntriesList.get(i);
            for (int j = 0; j < curValues.size(); ++j) {
                this.values[this.size] = curValues.get(j);
                this.articleEntries[this.size] = curArticleEntries.get(j);
                this.paragraphEntries[this.size] = curParagraphEntries.get(j);
                ++this.size;
            }
        }
    }

    @Override
    public FingerprintRepository build() {
        int[] index = new DualPivotQuicksort(this.values, this.size).sort();
        FingerprintRepository fingerprintRepository = new FingerprintRepositoryImpl(
                this.values, this.articleEntries, this.paragraphEntries, index,
                this.size);

        this.analyzer = null;
        this.values = null;
        this.articleEntries = null;
        this.paragraphEntries = null;
        this.size = 0;
        this.stringBuilder = null;
        this.fingerprintBuffer = null;
        return fingerprintRepository;
    }

    public static final FingerprintBuilder FINGERPRINT_BUILDER = new FingerprintBuilder();

    /**
     * Updated JDK class to support index based sort for memory efficiency
     * consideration. This class implements the Dual-Pivot Quicksort algorithm
     * by Vladimir Yaroslavskiy, Jon Bentley, and Josh Bloch. The algorithm
     * offers O(n log(n)) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance, and is typically faster
     * than traditional (one-pivot) Quicksort implementations.
     *
     * All exposed methods are package-private, designed to be invoked from
     * public methods (in class Arrays) after performing any necessary array
     * bounds checks and expanding parameters into the required forms.
     *
     * @author Vladimir Yaroslavskiy
     * @author Jon Bentley
     * @author Josh Bloch
     *
     * @version 2011.02.11 m765.827.12i:5\7pm
     * @since 1.7
     */
    static class DualPivotQuicksort {
        private final long[] values;
        private final int size;

        DualPivotQuicksort(long[] values, int size) {
            this.values = values;
            this.size = size;
        }

        int[] sort() {
            int[] a = new int[this.size];
            for (int i = 0; i < this.size; ++i) {
                a[i] = i;
            }

            if (this.size <= 1) {
                return a;
            }

            sort(a, 0, this.size - 1, null, 0, 0);
            return a;
        }

        private final long compare(int i, int j) {
            long v1 = this.values[i];
            long v2 = this.values[j];
            if (v1 >= 0) {
                if (v2 >= 0) {
                    return v1 - v2;
                }
                return 1;
            } else {
                if (v2 <= 0) {
                    return v1 - v2;
                }
                return -1;
            }
        }

        /*
         * Tuning parameters.
         */

        /**
         * The maximum number of runs in merge sort.
         */
        private static final int MAX_RUN_COUNT = 67;

        /**
         * The maximum length of run in merge sort.
         */
        private static final int MAX_RUN_LENGTH = 33;

        /**
         * If the length of an array to be sorted is less than this constant,
         * Quicksort is used in preference to merge sort.
         */
        private static final int QUICKSORT_THRESHOLD = 286;

        /**
         * If the length of an array to be sorted is less than this constant,
         * insertion sort is used in preference to Quicksort.
         */
        private static final int INSERTION_SORT_THRESHOLD = 47;

        /**
         * Sorts the specified range of the array using the given workspace
         * array slice if possible for merging
         *
         * @param a
         *            the array to be sorted
         * @param left
         *            the index of the first element, inclusive, to be sorted
         * @param right
         *            the index of the last element, inclusive, to be sorted
         * @param work
         *            a workspace array (slice)
         * @param workBase
         *            origin of usable space in work array
         * @param workLen
         *            usable size of work array
         */
        private void sort(int[] a, int left, int right, int[] work,
                int workBase, int workLen) {
            // Use Quicksort on small arrays
            if (right - left < QUICKSORT_THRESHOLD) {
                sort(a, left, right, true);
                return;
            }

            /*
             * Index run[i] is the start of i-th run (ascending or descending
             * sequence).
             */
            int[] run = new int[MAX_RUN_COUNT + 1];
            int count = 0;
            run[0] = left;

            // Check if the array is nearly sorted
            for (int k = left; k < right; run[count] = k) {
                if (this.compare(a[k], a[k + 1]) < 0) { // ascending
                    while (++k <= right && this.compare(a[k - 1], a[k]) <= 0)
                        ;
                } else if (this.compare(a[k], a[k + 1]) > 0) { // descending
                    while (++k <= right && this.compare(a[k - 1], a[k]) >= 0)
                        ;
                    for (int lo = run[count] - 1, hi = k; ++lo < --hi;) {
                        int t = a[lo];
                        a[lo] = a[hi];
                        a[hi] = t;
                    }
                } else { // equal
                    for (int m = MAX_RUN_LENGTH; ++k <= right
                            && this.compare(a[k - 1], a[k]) == 0;) {
                        if (--m == 0) {
                            sort(a, left, right, true);
                            return;
                        }
                    }
                }

                /*
                 * The array is not highly structured, use Quicksort instead of
                 * merge sort.
                 */
                if (++count == MAX_RUN_COUNT) {
                    sort(a, left, right, true);
                    return;
                }
            }

            // Check special cases
            // Implementation note: variable "right" is increased by 1.
            if (run[count] == right++) { // The last run contains one element
                run[++count] = right;
            } else if (count == 1) { // The array is already sorted
                return;
            }

            // Determine alternation base for merge
            byte odd = 0;
            for (int n = 1; (n <<= 1) < count; odd ^= 1)
                ;

            // Use or create temporary array b for merging
            int[] b; // temp array; alternates with a
            int ao, bo; // array offsets from 'left'
            int blen = right - left; // space needed for b
            if (work == null || workLen < blen || workBase + blen > work.length) {
                work = new int[blen];
                workBase = 0;
            }
            if (odd == 0) {
                System.arraycopy(a, left, work, workBase, blen);
                b = a;
                bo = 0;
                a = work;
                ao = workBase - left;
            } else {
                b = work;
                ao = 0;
                bo = workBase - left;
            }

            // Merging
            for (int last; count > 1; count = last) {
                for (int k = (last = 0) + 2; k <= count; k += 2) {
                    int hi = run[k], mi = run[k - 1];
                    for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                        if (q >= hi || p < mi
                                && this.compare(a[p + ao], a[q + ao]) <= 0) {
                            b[i + bo] = a[p++ + ao];
                        } else {
                            b[i + bo] = a[q++ + ao];
                        }
                    }
                    run[++last] = hi;
                }
                if ((count & 1) != 0) {
                    for (int i = right, lo = run[count - 1]; --i >= lo; b[i
                            + bo] = a[i + ao])
                        ;
                    run[++last] = right;
                }
                int[] t = a;
                a = b;
                b = t;
                int o = ao;
                ao = bo;
                bo = o;
            }
        }

        /**
         * Sorts the specified range of the array by Dual-Pivot Quicksort.
         *
         * @param a
         *            the array to be sorted
         * @param left
         *            the index of the first element, inclusive, to be sorted
         * @param right
         *            the index of the last element, inclusive, to be sorted
         * @param leftmost
         *            indicates if this part is the leftmost in the range
         */
        private void sort(int[] a, int left, int right, boolean leftmost) {
            int length = right - left + 1;

            // Use insertion sort on tiny arrays
            if (length < INSERTION_SORT_THRESHOLD) {
                if (leftmost) {
                    /*
                     * Traditional (without sentinel) insertion sort, optimized
                     * for server VM, is used in case of the leftmost part.
                     */
                    for (int i = left, j = i; i < right; j = ++i) {
                        int ai = a[i + 1];
                        while (this.compare(ai, a[j]) < 0) {
                            a[j + 1] = a[j];
                            if (j-- == left) {
                                break;
                            }
                        }
                        a[j + 1] = ai;
                    }
                } else {
                    /*
                     * Skip the longest ascending sequence.
                     */
                    do {
                        if (left >= right) {
                            return;
                        }
                    } while (this.compare(a[++left], a[left - 1]) >= 0);

                    /*
                     * Every element from adjoining part plays the role of
                     * sentinel, therefore this allows us to avoid the left
                     * range check on each iteration. Moreover, we use the more
                     * optimized algorithm, so called pair insertion sort, which
                     * is faster (in the context of Quicksort) than traditional
                     * implementation of insertion sort.
                     */
                    for (int k = left; ++left <= right; k = ++left) {
                        int a1 = a[k], a2 = a[left];

                        if (this.compare(a1, a2) < 0) {
                            a2 = a1;
                            a1 = a[left];
                        }
                        while (this.compare(a1, a[--k]) < 0) {
                            a[k + 2] = a[k];
                        }
                        a[++k + 1] = a1;

                        while (this.compare(a2, a[--k]) < 0) {
                            a[k + 1] = a[k];
                        }
                        a[k + 1] = a2;
                    }
                    int last = a[right];

                    while (this.compare(last, a[--right]) < 0) {
                        a[right + 1] = a[right];
                    }
                    a[right + 1] = last;
                }
                return;
            }

            // Inexpensive approximation of length / 7
            int seventh = (length >> 3) + (length >> 6) + 1;

            /*
             * Sort five evenly spaced elements around (and including) the
             * center element in the range. These elements will be used for
             * pivot selection as described below. The choice for spacing these
             * elements was empirically determined to work well on a wide
             * variety of inputs.
             */
            int e3 = (left + right) >>> 1; // The midpoint
            int e2 = e3 - seventh;
            int e1 = e2 - seventh;
            int e4 = e3 + seventh;
            int e5 = e4 + seventh;

            // Sort these elements using insertion sort
            if (this.compare(a[e2], a[e1]) < 0) {
                int t = a[e2];
                a[e2] = a[e1];
                a[e1] = t;
            }

            if (this.compare(a[e3], a[e2]) < 0) {
                int t = a[e3];
                a[e3] = a[e2];
                a[e2] = t;
                if (this.compare(t, a[e1]) < 0) {
                    a[e2] = a[e1];
                    a[e1] = t;
                }
            }
            if (this.compare(a[e4], a[e3]) < 0) {
                int t = a[e4];
                a[e4] = a[e3];
                a[e3] = t;
                if (this.compare(t, a[e2]) < 0) {
                    a[e3] = a[e2];
                    a[e2] = t;
                    if (this.compare(t, a[e1]) < 0) {
                        a[e2] = a[e1];
                        a[e1] = t;
                    }
                }
            }
            if (this.compare(a[e5], a[e4]) < 0) {
                int t = a[e5];
                a[e5] = a[e4];
                a[e4] = t;
                if (this.compare(t, a[e3]) < 0) {
                    a[e4] = a[e3];
                    a[e3] = t;
                    if (this.compare(t, a[e2]) < 0) {
                        a[e3] = a[e2];
                        a[e2] = t;
                        if (this.compare(t, a[e1]) < 0) {
                            a[e2] = a[e1];
                            a[e1] = t;
                        }
                    }
                }
            }

            // Pointers
            int less = left; // The index of the first element of center part
            int great = right; // The index before the first element of right
                               // part

            if (this.compare(a[e1], a[e2]) != 0
                    && this.compare(a[e2], a[e3]) != 0
                    && this.compare(a[e3], a[e4]) != 0
                    && this.compare(a[e4], a[e5]) != 0) {
                /*
                 * Use the second and fourth of the five sorted elements as
                 * pivots. These values are inexpensive approximations of the
                 * first and second terciles of the array. Note that pivot1 <=
                 * pivot2.
                 */
                int pivot1 = a[e2];
                int pivot2 = a[e4];

                /*
                 * The first and the last elements to be sorted are moved to the
                 * locations formerly occupied by the pivots. When partitioning
                 * is complete, the pivots are swapped back into their final
                 * positions, and excluded from subsequent sorting.
                 */
                a[e2] = a[left];
                a[e4] = a[right];

                /*
                 * Skip elements, which are less or greater than pivot values.
                 */
                while (this.compare(a[++less], pivot1) < 0)
                    ;
                while (this.compare(a[--great], pivot2) > 0)
                    ;

                /*
                 * Partitioning:
                 * 
                 * left part center part right part
                 * +----------------------------
                 * ----------------------------------+ | < pivot1 | pivot1 <= &&
                 * <= pivot2 | ? | > pivot2 |
                 * +----------------------------------
                 * ----------------------------+ ^ ^ ^ | | | less k great
                 * 
                 * Invariants:
                 * 
                 * all in (left, less) < pivot1 pivot1 <= all in [less, k) <=
                 * pivot2 all in (great, right) > pivot2
                 * 
                 * Pointer k is the first index of ?-part.
                 */
                outer: for (int k = less - 1; ++k <= great;) {
                    int ak = a[k];
                    if (this.compare(ak, pivot1) < 0) { // Move a[k] to left
                                                        // part
                        a[k] = a[less];
                        /*
                         * Here and below we use "a[i] = b; i++;" instead of
                         * "a[i++] = b;" due to performance issue.
                         */
                        a[less] = ak;
                        ++less;
                    } else if (this.compare(ak, pivot2) > 0) { // Move a[k] to
                                                               // right part
                        while (this.compare(a[great], pivot2) > 0) {
                            if (great-- == k) {
                                break outer;
                            }
                        }
                        if (this.compare(a[great], pivot1) < 0) { // a[great] <=
                                                                  // pivot2
                            a[k] = a[less];
                            a[less] = a[great];
                            ++less;
                        } else { // pivot1 <= a[great] <= pivot2
                            a[k] = a[great];
                        }
                        /*
                         * Here and below we use "a[i] = b; i--;" instead of
                         * "a[i--] = b;" due to performance issue.
                         */
                        a[great] = ak;
                        --great;
                    }
                }

                // Swap pivots into their final positions
                a[left] = a[less - 1];
                a[less - 1] = pivot1;
                a[right] = a[great + 1];
                a[great + 1] = pivot2;

                // Sort left and right parts recursively, excluding known pivots
                sort(a, left, less - 2, leftmost);
                sort(a, great + 2, right, false);

                /*
                 * If center part is too large (comprises > 4/7 of the array),
                 * swap internal pivot values to ends.
                 */
                if (this.compare(less, e1) < 0 && this.compare(e5, great) < 0) {
                    /*
                     * Skip elements, which are equal to pivot values.
                     */
                    while (this.compare(a[less], pivot1) == 0) {
                        ++less;
                    }

                    while (this.compare(a[great], pivot2) == 0) {
                        --great;
                    }

                    /*
                     * Partitioning:
                     * 
                     * left part center part right part
                     * +------------------------
                     * ----------------------------------+ | == pivot1 | pivot1
                     * < && < pivot2 | ? | == pivot2 |
                     * +--------------------------
                     * --------------------------------+ ^ ^ ^ | | | less k
                     * great
                     * 
                     * Invariants:
                     * 
                     * all in (*, less) == pivot1 pivot1 < all in [less, k) <
                     * pivot2 all in (great, *) == pivot2
                     * 
                     * Pointer k is the first index of ?-part.
                     */
                    outer: for (int k = less - 1; ++k <= great;) {
                        int ak = a[k];
                        if (this.compare(ak, pivot1) == 0) { // Move a[k] to
                                                             // left part
                            a[k] = a[less];
                            a[less] = ak;
                            ++less;
                        } else if (this.compare(ak, pivot2) == 0) { // Move a[k]
                                                                    // to right
                                                                    // part
                            while (this.compare(a[great], pivot2) == 0) {
                                if (great-- == k) {
                                    break outer;
                                }
                            }
                            if (this.compare(a[great], pivot1) == 0) { // a[great]
                                                                       // <
                                                                       // pivot2
                                a[k] = a[less];
                                /*
                                 * Even though a[great] equals to pivot1, the
                                 * assignment a[less] = pivot1 may be incorrect,
                                 * if a[great] and pivot1 are floating-point
                                 * zeros of different signs. Therefore in float
                                 * and double sorting methods we have to use
                                 * more accurate assignment a[less] = a[great].
                                 */
                                a[less] = pivot1;
                                ++less;
                            } else { // pivot1 < a[great] < pivot2
                                a[k] = a[great];
                            }
                            a[great] = ak;
                            --great;
                        }
                    }
                }

                // Sort center part recursively
                sort(a, less, great, false);

            } else { // Partitioning with one pivot
                /*
                 * Use the third of the five sorted elements as pivot. This
                 * value is inexpensive approximation of the median.
                 */
                int pivot = a[e3];

                /*
                 * Partitioning degenerates to the traditional 3-way (or
                 * "Dutch National Flag") schema:
                 * 
                 * left part center part right part
                 * +-------------------------------------------------+ | < pivot
                 * | == pivot | ? | > pivot |
                 * +-------------------------------------------------+ ^ ^ ^ | |
                 * | less k great
                 * 
                 * Invariants:
                 * 
                 * all in (left, less) < pivot all in [less, k) == pivot all in
                 * (great, right) > pivot
                 * 
                 * Pointer k is the first index of ?-part.
                 */
                for (int k = less; k <= great; ++k) {
                    if (this.compare(a[k], pivot) == 0) {
                        continue;
                    }
                    int ak = a[k];
                    if (this.compare(ak, pivot) < 0) { // Move a[k] to left part
                        a[k] = a[less];
                        a[less] = ak;
                        ++less;
                    } else { // a[k] > pivot - Move a[k] to right part
                        while (this.compare(a[great], pivot) > 0) {
                            --great;
                        }
                        if (this.compare(a[great], pivot) < 0) { // a[great] <=
                                                                 // pivot
                            a[k] = a[less];
                            a[less] = a[great];
                            ++less;
                        } else { // a[great] == pivot
                            /*
                             * Even though a[great] equals to pivot, the
                             * assignment a[k] = pivot may be incorrect, if
                             * a[great] and pivot are floating-point zeros of
                             * different signs. Therefore in float and double
                             * sorting methods we have to use more accurate
                             * assignment a[k] = a[great].
                             */
                            a[k] = pivot;
                        }
                        a[great] = ak;
                        --great;
                    }
                }

                /*
                 * Sort left and right parts recursively. All elements from
                 * center part are equal and, therefore, already sorted.
                 */
                sort(a, left, less - 1, leftmost);
                sort(a, great + 1, right, false);
            }
        }

    }

}
