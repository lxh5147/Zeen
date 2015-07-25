package com.zeen.plagiarismchecker;

public interface ContentAnalyzer {
    /**
     * 
     * @param content
     *            text to be analyzed
     * @return a list of check point list
     */
    Iterable<Iterable<CharSequence>> analyze(final String content);

    static final int MAX_LENGTH_OF_CHECKPOINTS_LIST_PER_ANALYZER = 256;
}
