package com.zeen.plagiarismchecker.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

public class ArticleRepositoryTestUtil {
    private ArticleRepositoryTestUtil() {
    }

    public static final String[][] ARTICLES = new String[][] {
            {
                    "this work uses deep learning to represent a reference. we can summurize our contributions as follows.",
                    "deep learning has attracted increasing interestes in recent studies on nlp. it has been widely applied to word embedding, language modeling." },
            { "it is still not clear if and how deep learning can help to detect repeated text. In this work, we design a mapping from a paragraph to a paragraph vector, which is used to detect copy&past text." },
            {
                    "experimental results show our method outperforms the method based on bag of words.",
                    "in future we will apply our method to other languages, such as chinese." } };

    public static final String[] FOLDERS = { "ref1", "ref2" };

    public static void setupArticleRepository() throws IOException {
        // create two folders
        new File(FOLDERS[0]).mkdirs();
        write(new File(FOLDERS[0] + "/0"), ARTICLES[0]);
        write(new File(FOLDERS[0] + "/1"), ARTICLES[1]);
        new File(FOLDERS[1]).mkdirs();
        write(new File(FOLDERS[1] + "/2"), ARTICLES[2]);
    }

    private static void write(File file, String[] lines)
            throws UnsupportedEncodingException, FileNotFoundException,
            IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"))) {
            for (int i = 0; i < lines.length; ++i) {
                writer.write(lines[i]);
                writer.newLine();
            }
        }
    }

    public static void tearDownArticleRepository() {
        new File(FOLDERS[0] + "/0").delete();
        new File(FOLDERS[0] + "/1").delete();
        new File(FOLDERS[0]).delete();
        new File(FOLDERS[1] + "/2").delete();
        new File(FOLDERS[1]).delete();
    }
}
