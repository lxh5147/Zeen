package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.Charset;
import java.util.List;

public class FingerprintBuilder {

    FingerprintBuilder() {
        this.charset = Charset.forName("UTF-8");
    }

    public long getFingerprint(String content) {
        checkNotNull(content, "content");
        return fingerprint64(content.getBytes(this.charset));
    }

    public void buildFingerprints(List<Iterable<CharSequence>> checkPointsList,
            StringBuilder stringBuilder, long[] fingerprints) {
        checkNotNull(checkPointsList, "checkPointsList");
        checkNotNull(stringBuilder, "stringBuilder");
        checkNotNull(fingerprints, "fingerprints");
        checkArgument(fingerprints.length >= checkPointsList.size(),
                "fingerprints");
        for (int i = 0; i < checkPointsList.size(); ++i) {
            stringBuilder.setLength(0);
            for (CharSequence checkPoint : checkPointsList.get(i)) {
                stringBuilder.append(checkPoint);
            }
            fingerprints[i] = getFingerprint(stringBuilder.toString());
        }
    }


    private final Charset charset;

    // reference:
    // http://grepcode.com/file/repo1.maven.org/maven2/org.apache.avro/avro/1.7.0/org/apache/avro/SchemaNormalization.java#SchemaNormalization.FP64.0FP_TABLE
    private static long fingerprint64(byte[] data) {
        long result = EMPTY64;
        for (byte b : data)
            result = (result >>> 8) ^ FP64.FP_TABLE[(int) (result ^ b) & 0xff];
        return result;
    }

    private static long EMPTY64 = 0xc15d213aa4d7a795L;

    private static class FP64 {
        private static final long[] FP_TABLE = new long[256];
        static {
            for (int i = 0; i < 256; i++) {
                long fp = i;
                for (int j = 0; j < 8; j++) {
                    long mask = -(fp & 1L);
                    fp = (fp >>> 1) ^ (EMPTY64 & mask);
                }
                FP_TABLE[i] = fp;
            }
        }
    }

}
