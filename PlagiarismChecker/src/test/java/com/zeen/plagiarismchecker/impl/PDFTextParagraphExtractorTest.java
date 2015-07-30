package com.zeen.plagiarismchecker.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import jersey.repackaged.com.google.common.collect.Lists;
import junit.framework.Assert;

import org.junit.Test;

public class PDFTextParagraphExtractorTest {

    private static final Logger LOGGER = Logger
            .getLogger(PDFTextParagraphExtractorTest.class.getName());

    @Test
    public void extractFromPDFManualTest() throws IOException {
        Lists.newArrayList(new File("testFiles/566.pdf"),
                new File("testFiles/1003.pdf"), new File("testFiles/381.pdf"))
                .forEach(
                        file -> {
                            LOGGER.info(String
                                    .format("=====================extract %s=====================",
                                            file.getAbsolutePath()));
                            String text;
                            try {
                                text = PDFTextExtractor.extract(file);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            Iterable<String> paragraphs = PDFTextParagraphExtractor
                                    .extract(text);
                            paragraphs.forEach(line -> {
                                LOGGER.info(line);

                            });
                            Assert.assertNotNull(paragraphs);
                        });
    }

    @Test
    public void extractFromPDFTextManualTest() {
        Lists.newArrayList(new File("testFiles/566.txt"),
                new File("testFiles/1003.txt"), new File("testFiles/381.txt"))
                .forEach(
                        file -> {
                            LOGGER.info(String
                                    .format("=====================extract %s=====================",
                                            file.getAbsolutePath()));
                            String text;
                            try {
                                text = readTxtFile(file);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            Iterable<String> paragraphs = PDFTextParagraphExtractor
                                    .extract(text);
                            paragraphs.forEach(line -> {
                                LOGGER.info(line);
                            });
                            Assert.assertNotNull(paragraphs);
                        });
    }

    private static String readTxtFile(File file) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF8"))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }

            return stringBuilder.toString();
        }
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

    @Test
    public void extractCase2Test() {
        String text = "Table 2 Regression coefficients (and standard errors) across datasets to evaluate average score and career average score ..........................  28";
        Assert.assertTrue(Lists.newArrayList(
                PDFTextParagraphExtractor.extract(text)).isEmpty());

    }
    //
}
