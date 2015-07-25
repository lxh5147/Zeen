package com.zeen.plagiarismchecker;

public interface Article {
    int getId();

    String getDOI();

    String getTitle();

    String getAuthor();

    String getAbstraction();

    Iterable<Paragraph> getParagraphs();

    interface Builder {
        Article build();

        Builder withId(int id);

        Builder withDOI(String doi);

        Builder withTitle(String title);

        Builder withAbstract(String abstraction);

        Builder withParagraphs(Iterable<Paragraph> paragraphs);
    }
}
