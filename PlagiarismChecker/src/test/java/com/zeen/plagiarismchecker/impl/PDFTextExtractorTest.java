package com.zeen.plagiarismchecker.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.zeen.plagiarismchecker.impl.PDFTextExtractor;

public class PDFTextExtractorTest {
    @Test
    public void extractTest() throws IOException {
        String text = PDFTextExtractor.extract(new File("testFiles/1003.pdf"));
        Assert.assertTrue(text
                .startsWith("EVALUATION OF TRAITS ASSOCIATED WITH BUCKING BULL"));
        Assert.assertTrue(text.trim().endsWith("December 2005"));
    }
}
