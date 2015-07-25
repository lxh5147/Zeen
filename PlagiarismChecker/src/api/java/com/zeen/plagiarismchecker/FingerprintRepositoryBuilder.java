package com.zeen.plagiarismchecker;


public interface FingerprintRepositoryBuilder {

    void start(ContentAnalyzer analyzer, int capability);

    void add(Paragraph paragraph);

    void add(Iterable<Paragraph> paragraphs, int parallelism);

    FingerprintRepository build();
}
