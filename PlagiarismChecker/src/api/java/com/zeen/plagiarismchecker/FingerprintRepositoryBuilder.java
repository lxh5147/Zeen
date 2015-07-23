package com.zeen.plagiarismchecker;

public interface FingerprintRepositoryBuilder {

		void start(ContentAnalyzer analyzer, int capability);

		void add(Paragraph paragraph);

		FingerprintRepository build();	
}
