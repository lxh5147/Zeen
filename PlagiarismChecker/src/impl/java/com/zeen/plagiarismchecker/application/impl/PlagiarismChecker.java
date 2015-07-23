package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zeen.plagiarismchecker.FingerprintRepository;
import com.zeen.plagiarismchecker.ParagraphEntry;
import com.zeen.plagiarismchecker.impl.ContentAnalizers;
import com.zeen.plagiarismchecker.impl.FingerprintRepositoryBuilderImpl;
import com.zeen.plagiarismchecker.impl.FingerprintRepositoryImpl;

public class PlagiarismChecker {
    final List<FingerprintRepository> fingerprintRepositories;
    final List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList;

    public  PlagiarismChecker(
            List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList)
            throws IOException {
        checkNotNull(fingerprintRepositoryInfoList,"fingerprintRepositoryInfoList");
        
        this.fingerprintRepositoryInfoList = Lists
                .newArrayList(fingerprintRepositoryInfoList);
        this.fingerprintRepositories = Lists
                .newArrayListWithCapacity(fingerprintRepositoryInfoList.size());
        this.loadIndexes();
    }

     
    private void loadIndexes() throws IOException {
        for (int i = 0; i < this.fingerprintRepositoryInfoList.size(); ++i) {
            this.fingerprintRepositories.add(FingerprintRepositoryImpl
                    .load(this.fingerprintRepositoryInfoList.get(i).indexFile));
        }
    }

    public Iterable<ParagraphEntry> check(String paragraph) {
        checkNotNull(paragraph, "paragraph");

        List<Iterable<ParagraphEntry>> resultsList = Lists
                .newArrayListWithCapacity(this.fingerprintRepositories.size());
        for (int i = 0; i < this.fingerprintRepositories.size(); ++i) {
            resultsList.add(null);
        }
        // check multiple indexes in parallel
        IntStream
                .range(0, this.fingerprintRepositoryInfoList.size())
                .parallel()
                .forEach(
                        i -> {
                            resultsList
                                    .set(i,
                                            this.fingerprintRepositories
                                                    .get(i)
                                                    .getFingerprintEntries(
                                                            FingerprintRepositoryImpl
                                                                    .newFingerprint(FingerprintRepositoryBuilderImpl.FINGERPRINT_BUILDER
                                                                            .getFingerprint(
                                                                                    paragraph,
                                                                                    this.fingerprintRepositoryInfoList
                                                                                            .get(i).contentAnalizer,
                                                                                    new StringBuilder()))));
                        });
        // return the merged results
        Set<ParagraphEntry> mergedResults = Sets.newHashSet();
        resultsList.forEach(results -> {
            results.forEach(paragraphEntry -> {
                mergedResults.add(paragraphEntry);
            });

        });
        return mergedResults;
    }
    
    static PlagiarismChecker getPlagiarismCheckerWithArgs(String[] args)
            throws ParseException, IOException {
        // build CLI
        // -a --contentAnalizers name1,name2,...namen
        // -i --indexPath path

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(
                Option.builder("a").argName("names").hasArg().required()
                        .longOpt("contentAnalizers")
                        .desc("content analizer names, separated by comma")
                        .build())
                .addOption(
                        Option.builder("i")
                                .argName("path")
                                .hasArg()
                                .required()
                                .longOpt("indexPath")
                                .desc("index path, each content analizer will create an index under this path")
                                .build());

        CommandLine line = parser.parse(options, args);

        List<String> contentAnalizerNames = Lists.newArrayList(Splitter.on(',')
                .split(line.getOptionValue("contentAnalizers")));
        Path indexPath = Paths.get(line.getOptionValue("indexPath"));
        checkArgument(indexPath.toFile().exists() && indexPath.toFile().isDirectory(),"indexPath");
        

        List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList = Lists
                .newArrayListWithCapacity(contentAnalizerNames.size());

        contentAnalizerNames
                .forEach(name -> {
                    // index file must exist
                    File indexFile = indexPath.resolve(name).toFile();
                    checkArgument(
                            indexFile.exists() && !indexFile.isDirectory(),
                            "indexFile");
                    fingerprintRepositoryInfoList
                            .add(new FingerprintRepositoryInfo(ContentAnalizers
                                    .valueOf(name).getContentAnalizer(),
                                    indexFile));
                });

        if (!indexPath.toFile().exists()) {
            indexPath.toFile().mkdirs();
        }

        return new PlagiarismChecker(fingerprintRepositoryInfoList);
    }

    public static void main(final String[] args) throws ParseException,
            IOException {
        PlagiarismChecker plagiarismChecker = getPlagiarismCheckerWithArgs(args);
        try (Scanner scan = new Scanner(System.in)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if (line.equals("")) {
                    break;
                }
                System.out.println(plagiarismChecker.check(line));
            }
        }
    }
}
