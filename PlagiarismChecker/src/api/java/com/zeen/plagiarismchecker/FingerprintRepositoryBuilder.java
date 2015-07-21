package com.zeen.plagiarismchecker;

public interface FingerprintRepositoryBuilder {

		void start(ContentAnalizer analizer, int capability);

		void add(Paragraph paragraph);

		FingerprintRepository build();	
}
