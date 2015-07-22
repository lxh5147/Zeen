package com.zeen.plagiarismchecker;

import java.io.File;
import java.io.IOException;

public interface FingerprintRepository {
    Iterable<ParagraphEntry> getFingerprintEntries(Fingerprint fingerprint);

    void save(File file) throws IOException;
}
