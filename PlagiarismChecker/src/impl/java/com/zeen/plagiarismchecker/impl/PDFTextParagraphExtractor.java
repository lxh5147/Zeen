package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public class PDFTextParagraphExtractor {
    private static final Splitter TOKEN_SPLITTER = Splitter
            .on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).trimResults()
            .omitEmptyStrings();
    private static final Splitter LINE_SPLITTER = Splitter.on('\n')
            .trimResults().omitEmptyStrings();
    private static int MINIMAL_TOKENS_PER_LINE = 5;
    private static int MINIMAL_TOKENS_PER_PARAGRAPH = 12;

    private PDFTextParagraphExtractor() {
    }

    private static final CharMatcher SENTENCE_END_INDICATOR = CharMatcher
            .is('.').or(CharMatcher.is('?')).or(CharMatcher.is('!'));
    private static final CharMatcher PARTIAL_SENTENCE_END_INDICATOR = CharMatcher
            .is(',').or(CharMatcher.is(':')).or(CharMatcher.is('"'));

    public static Iterable<String> extract(String content) {
        checkNotNull(content, "content");
        List<String> lines = LINE_SPLITTER.splitToList(content);
        List<String> paragraphs = Lists.newArrayList();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            List<String> tokens = TOKEN_SPLITTER.splitToList(line);
            if (tokens.isEmpty()) {
                continue;
            }
            List<String> nonNumericTokens = Lists
                    .newArrayListWithCapacity(tokens.size());
            tokens.forEach(token -> {
                if (!isNumeric(token)) {
                    nonNumericTokens.add(token);
                }
            });
            // if current line does not start with a upper case character, and
            // current paragraph is not ending with sentence end indicator,
            // append it to current paragraph
            String firstToken = tokens.get(0);
            if (!CharMatcher.JAVA_UPPER_CASE.matches(firstToken.charAt(0))
                    && (stringBuilder.length() == 0 || !SENTENCE_END_INDICATOR
                            .matches(stringBuilder.charAt(stringBuilder
                                    .length() - 1)))) {
                if (nonNumericTokens.size() > MINIMAL_TOKENS_PER_LINE) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(' ');
                    }
                    stringBuilder.append(line);
                    continue;
                }
                // special case
                // current line ends with sentence end indicator, but current
                // paragraph not, still append to current paragraph
                if (SENTENCE_END_INDICATOR
                        .matches(line.charAt(line.length() - 1))) {
                    if (stringBuilder.length() > 0) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(line);
                    }
                }
                continue;
            }
            // create a new paragraph
            if (stringBuilder.length() > 0) {
                paragraphs.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
            // append current line if and only if it has more than
            // MINIMAL_TOKENS_PER_LINE non numeric tokens
            if (nonNumericTokens.size() > MINIMAL_TOKENS_PER_LINE) {
                stringBuilder.append(line);
            }
        }
        if (stringBuilder.length() > 0) {
            paragraphs.add(stringBuilder.toString());
            stringBuilder.setLength(0);
        }
        // merge adjacent paragraphs if possible
        List<String> mergedParagraphs = Lists
                .newArrayListWithCapacity(paragraphs.size());
        for (String paragraph : paragraphs) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(paragraph);
            // create a new paragraph if current paragraph is not ending with
            // ','
            if (!PARTIAL_SENTENCE_END_INDICATOR.matches(stringBuilder
                    .charAt(stringBuilder.length() - 1))) {
                // create a new paragraph
                mergedParagraphs.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
        if (stringBuilder.length() > 0) {
            mergedParagraphs.add(stringBuilder.toString());
        }
        return postMergeParagraphs(postFilterParagraphs(mergedParagraphs));
    }

    private static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    private static List<String> postFilterParagraphs(List<String> paragraphs) {
        List<String> results = Lists
                .newArrayListWithCapacity(paragraphs.size());
        for (String paragraph : paragraphs) {
            List<String> tokens = TOKEN_SPLITTER.splitToList(paragraph);
            int tokenCount = tokens.size();
            if (tokenCount == 0) {
                continue;
            }
            List<String> functionalTokens = getFunctionalTokens(paragraph);
            int numericTokenCount = 0;

            for (String token : tokens) {
                if (isNumeric(token)) {
                    ++numericTokenCount;
                }
            }
            // to handle continuous ...,-
            int functionalTokenCount = 0;
            for (String token : functionalTokens) {
                functionalTokenCount += token.length();
            }
            // filter reference
            if (isReferenceParagraph(tokenCount, numericTokenCount,
                    functionalTokenCount)) {
                continue;
            }
            // filter short incomplete paragraph
            if (tokenCount - numericTokenCount < MINIMAL_TOKENS_PER_PARAGRAPH) {
                if (!SENTENCE_END_INDICATOR.matches(paragraph.charAt(paragraph
                        .length() - 1))) {
                    continue;
                }
            }
            // filter paragraph long enough, but not containing any sentence end
            // indicator
            if (SENTENCE_END_INDICATOR_SPLITTER.splitToList(paragraph)
                    .isEmpty()) {
                continue;
            }
            results.add(paragraph);
        }
        return results;
    }

    private static List<String> getFunctionalTokens(String paragraph) {
        List<String> functionalTokens = Lists.newArrayList();
        // ignore functional tokens between '(' and ')'
        StringBuilder stringBuilder = new StringBuilder();
        int curPos = 0;
        int nextPos = -1;
        while (curPos < paragraph.length()) {
            nextPos = paragraph.indexOf(curPos, '(');
            if (nextPos == -1) {
                nextPos = paragraph.length();
            }
            if (nextPos > curPos) {
                stringBuilder.append(paragraph, curPos, nextPos);
                functionalTokens.addAll(FUNCTIONAL_TOKEN_SPLITTER
                        .splitToList(stringBuilder));
                stringBuilder.setLength(0);
            }
            if (nextPos < paragraph.length()) {
                nextPos = paragraph.indexOf(nextPos, ')');
            }
            if (nextPos == -1) {
                nextPos = paragraph.length();
            }
            curPos = nextPos + 1;
        }
        return functionalTokens;
    }

    private static List<String> postMergeParagraphs(List<String> paragraphs) {
        List<String> results = Lists
                .newArrayListWithCapacity(paragraphs.size());
        // ensure that each paragraph has a valid ending character
        StringBuilder stringBuilder = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(paragraph);
            if (SENTENCE_END_INDICATOR.matches(stringBuilder
                    .charAt(stringBuilder.length() - 1))) {
                results.add(stringBuilder.toString());
                stringBuilder.setLength(0);
            }
        }
        if (stringBuilder.length() > 0) {
            results.add(stringBuilder.toString());
        }
        return results;
    }

    private static final Splitter FUNCTIONAL_TOKEN_SPLITTER = Splitter
            .on(CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.is('('))
                    .or(CharMatcher.is(')')).or(CharMatcher.is('%')))
            .trimResults().omitEmptyStrings();

    private static final Splitter SENTENCE_END_INDICATOR_SPLITTER = Splitter
            .on(SENTENCE_END_INDICATOR.negate()).trimResults()
            .omitEmptyStrings();

    private static final double NON_WORD_RATIO_THRESHOLD_FOR_REFERENCE = 0.25;

    private static boolean isReferenceParagraph(int tokenCount,
            int numericTokenCount, int functionalTokenCount) {
        double functionalAndNumericTokenRatio = (functionalTokenCount + numericTokenCount)
                / (double) (tokenCount);
        return functionalAndNumericTokenRatio >= NON_WORD_RATIO_THRESHOLD_FOR_REFERENCE;
    }
}
