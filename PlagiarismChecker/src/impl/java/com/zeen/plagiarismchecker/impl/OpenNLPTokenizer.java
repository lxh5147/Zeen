package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.collect.Lists;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class OpenNLPTokenizer implements Tokenizer {

    // tokenizer is not thread safe
    private static final ThreadLocal<opennlp.tools.tokenize.Tokenizer> _TOKENIZER = new ThreadLocal<opennlp.tools.tokenize.Tokenizer>() {
        @Override
        protected opennlp.tools.tokenize.Tokenizer initialValue() {
            InputStream is = this.getClass().getResourceAsStream(
                    "/en-token.bin");
            TokenizerModel model;
            try {
                model = new TokenizerModel(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            opennlp.tools.tokenize.Tokenizer tokenizer = new TokenizerME(model);
            return tokenizer;
        }
    };

    @Override
    public Iterable<? extends CharSequence> split(String content) {
        checkNotNull(content, "content");
        return Lists.newArrayList(_TOKENIZER.get().tokenize(content));
    }

}
