package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import jersey.repackaged.com.google.common.collect.Lists;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryImpl;
import com.zeen.plagiarismchecker.impl.PDFTextParagraphExtractor;

public class ArticleRepositoryBuilder {
    final List<Path> pdfTextFileFolders;
    final Path articleRepositoryFolder;
    final boolean overwrite;
    private static final Logger LOGGER = Logger
            .getLogger(ArticleRepositoryBuilder.class.getName());

    private ArticleRepositoryBuilder(List<Path> pdfTextFileFolders,
            Path articleRepositoryFolder, boolean overwrite) {
        this.pdfTextFileFolders = pdfTextFileFolders;
        this.articleRepositoryFolder = articleRepositoryFolder;
        this.overwrite = overwrite;
    }

    public void build() throws IOException {
        pdfTextFileFolders
                .stream()
                .parallel()
                .forEach(
                        pdfTextFilesPath -> {
                            Lists.newArrayList(
                                    ArticleRepositoryImpl
                                            .getFiles(pdfTextFilesPath))
                                    .parallelStream()
                                    .forEach(
                                            path -> {
                                                File file = path.toFile();
                                                String fileName = file
                                                        .getName();
                                                if (!Files.getFileExtension(
                                                        fileName).equals("txt")) {
                                                    LOGGER.log(
                                                            Level.WARNING,
                                                            String.format(
                                                                    "Ignore PDF text file %s, since its extension is not txt",
                                                                    file.getAbsolutePath()));
                                                    return;
                                                }
                                                File outputFile = this.articleRepositoryFolder
                                                        .resolve(
                                                                Files.getNameWithoutExtension(fileName))
                                                        .toFile();
                                                // in not overwrite mode, if the
                                                // output file exists, not write
                                                if (!overwrite) {
                                                    if (outputFile.exists()
                                                            && outputFile
                                                                    .isFile()) {
                                                        LOGGER.log(
                                                                Level.WARNING,
                                                                String.format(
                                                                        "Ignore PDF text file %s, since file %s already exists",
                                                                        file.getAbsolutePath(),
                                                                        outputFile
                                                                                .getAbsolutePath()));
                                                        return;
                                                    }
                                                }
                                                LOGGER.info(String
                                                        .format("Converting PDF text file %s to gzipped file %s",
                                                                file.getAbsolutePath(),
                                                                outputFile
                                                                        .getAbsolutePath()));

                                                writeGzippedFile(
                                                        outputFile,
                                                        PDFTextParagraphExtractor
                                                                .extract(readTxtFile(file)));
                                            });
                        });
    }

    private static String readTxtFile(File file) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF8"))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            LOGGER.log(
                    Level.SEVERE,
                    String.format("readTextFile %s caught exception",
                            file.getAbsolutePath()), e);
            throw new RuntimeException(e);
        }
    }

    private static void writeGzippedFile(File file, Iterable<String> paragraphs) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"))) {
            for (String paragraph : paragraphs) {
                writer.write(paragraph);
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(
                    Level.SEVERE,
                    String.format("writeGzippedFile %s caught exception",
                            file.getAbsolutePath()), e);
            throw new RuntimeException(e);
        }
    }

    static ArticleRepositoryBuilder getArticleRepositoryBuilderWithArgs(
            String[] args) throws ParseException, IOException {

        // -p --pdfTextFileFolders path1,path2,...pathn
        // -a --articleRepositoryFolder path
        // -o --overwrite

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(
                Option.builder("p")
                        .argName("paths")
                        .hasArg()
                        .required()
                        .longOpt("pdfTextFileFolders")
                        .desc("paths of pdf text file folders, separated by comma")
                        .build())
                .addOption(
                        Option.builder("a").argName("path").hasArg().required()
                                .longOpt("articleRepositoryFolder")
                                .desc("path of article repository").build())
                .addOption(
                        Option.builder("o").longOpt("overwrite")
                                .desc("overwrite an existing article").build());

        CommandLine line = parser.parse(options, args);
        List<String> pdfTextFileFolders = Lists.newArrayList(Splitter.on(',')
                .split(line.getOptionValue("pdfTextFileFolders")));

        Path articleRepositoryFolder = Paths.get(line
                .getOptionValue("articleRepositoryFolder"));

        checkArgument(!articleRepositoryFolder.toFile().exists()
                || articleRepositoryFolder.toFile().isDirectory(),
                "articleRepositoryFolder");

        List<Path> folders = Lists.newArrayListWithCapacity(pdfTextFileFolders
                .size());
        pdfTextFileFolders.forEach(folder -> {
            File file = new File(folder);
            checkArgument(file.exists() && file.isDirectory(),
                    "pdf text file folder");
            folders.add(Paths.get(folder));
        });

        if (!articleRepositoryFolder.toFile().exists()) {
            articleRepositoryFolder.toFile().mkdirs();
        }

        boolean overwrite = line.hasOption("overwrite");

        return new ArticleRepositoryBuilder(folders, articleRepositoryFolder,
                overwrite);
    }

    public static void main(final String[] args) throws ParseException,
            IOException {
        getArticleRepositoryBuilderWithArgs(args).build();
    }
}
