package com.zeen.plagiarismchecker.application.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class PDFTextExtractorTest {
    @Test
    public void extractTest() throws IOException {
        String text = PDFTextExtractor.extract(new File("1003.pdf"));
        Assert.assertTrue(text
                .startsWith("EVALUATION OF TRAITS ASSOCIATED WITH BUCKING BULL"));
        Assert.assertTrue(text.trim().endsWith("December 2005"));
    }
}
