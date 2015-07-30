package com.zeen.plagiarismchecker.application.impl;

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

    private PDFTextParagraphExtractor() {
    }

    private static final CharMatcher SENTENCE_END_INDICATOR = CharMatcher
            .is('.').or(CharMatcher.is('?')).or(CharMatcher.is('!'));

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
            // if current line does not start with a upper case character,
            // append it to current paragraph
            String firstToken = tokens.get(0);
            if (!CharMatcher.JAVA_UPPER_CASE.matches(firstToken.charAt(0))) {
                if (nonNumericTokens.size() > MINIMAL_TOKENS_PER_LINE) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(' ');
                    }
                    stringBuilder.append(line);
                }
                // special case
                // current line is end with sentence end indicator, but current
                // paragraph not, still append to current paragraph
                if (SENTENCE_END_INDICATOR
                        .matches(line.charAt(line.length() - 1))) {
                    if (stringBuilder.length() > 0) {
                        if (!SENTENCE_END_INDICATOR.matches(stringBuilder
                                .charAt(stringBuilder.length() - 1))) {
                            if (stringBuilder.length() > 0) {
                                stringBuilder.append(' ');
                            }
                            stringBuilder.append(line);
                        }
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
            if (stringBuilder.length() == 0) {
                stringBuilder.append(paragraph);
            }
            if (SENTENCE_END_INDICATOR.matches(stringBuilder
                    .charAt(stringBuilder.length() - 1))) {
                mergedParagraphs.add(stringBuilder.toString());
                stringBuilder.setLength(0);
                continue;
            }
            // other case we should not merge
            stringBuilder.append(' ');
            stringBuilder.append(paragraph);
        }
        if (stringBuilder.length() > 0) {
            mergedParagraphs.add(stringBuilder.toString());
        }
        return mergedParagraphs;
    }

    private static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

}
