package com.zeen.plagiarismchecker;

import java.util.List;

public interface FingerprintRepositoryBuilder {

    void start(ContentAnalyzer analyzer, int capability);

    void add(Paragraph paragraph);

    void add(List<Paragraph> paragraphList, int parallelism);

    FingerprintRepository build();
}
