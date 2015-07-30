package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFTextExtractor {
    private PDFTextExtractor() {
    }

    public static String extract(File pdfFile) throws IOException {
        checkNotNull(pdfFile, "pdfFile");
        PDFParser parser = new PDFParser(new FileInputStream(pdfFile));
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper pdfStripper = new PDFTextStripper();
        PDDocument pdDoc = new PDDocument(cosDoc);
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(pdDoc.getNumberOfPages());
        pdfStripper.setSortByPosition(true);
        String pdfText = pdfStripper.getText(pdDoc);
        pdDoc.close();
        cosDoc.close();
        return pdfText;
    }
}
