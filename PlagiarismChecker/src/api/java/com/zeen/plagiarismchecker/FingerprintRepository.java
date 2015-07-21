package com.zeen.plagiarismchecker;

public interface FingerprintRepository {
	Iterable<ParagraphEntry> getFingerprintEntries(Fingerprint fingerprint);	
}
