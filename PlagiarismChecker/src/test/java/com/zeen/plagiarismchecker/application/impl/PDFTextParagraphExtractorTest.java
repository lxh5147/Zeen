package com.zeen.plagiarismchecker.application.impl;

import java.io.File;
import java.io.IOException;

import jersey.repackaged.com.google.common.collect.Lists;
import junit.framework.Assert;

import org.junit.Test;

public class PDFTextParagraphExtractorTest {
    @Test
    public void extractTest() throws IOException {
        String text = PDFTextExtractor.extract(new File("1003.pdf"));
        Iterable<String> paragraphs = PDFTextParagraphExtractor.extract(text);
        paragraphs.forEach(line -> {
            System.out.println(line);
            System.out.println();
        });
        Assert.assertNotNull(paragraphs);
    }

    @Test
    public void extractCaseTest() {
        String text = "INTRODUCTION\n"
                + "The sport of bull riding has become the fastest growing professional sporting\n"
                + "        event in the United States (PBR, 2008) and showcases the combined athletic performance of both the bull and the rider.";
        Assert.assertEquals(
                Lists.newArrayList("The sport of bull riding has become the fastest growing professional sporting event in the United States (PBR, 2008) and showcases the combined athletic performance of both the bull and the rider."),
                PDFTextParagraphExtractor.extract(text));

    }
}
