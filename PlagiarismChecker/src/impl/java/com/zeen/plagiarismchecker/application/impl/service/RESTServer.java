package com.zeen.plagiarismchecker.application.impl.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zeen.plagiarismchecker.Article;
import com.zeen.plagiarismchecker.ArticleRepository;
import com.zeen.plagiarismchecker.ContentAnalyzer;
import com.zeen.plagiarismchecker.Paragraph;
import com.zeen.plagiarismchecker.ParagraphEntry;
import com.zeen.plagiarismchecker.application.impl.FingerprintRepositoryInfo;
import com.zeen.plagiarismchecker.application.impl.PlagiarismChecker;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryImpl;
import com.zeen.plagiarismchecker.impl.ContentAnalyzerType;

public class RESTServer {

    public static void main(String[] args) throws Exception {
        setupContext(args);

        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                PlagiarismCheckeService.class.getCanonicalName());

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

    static void setupContext(String[] args) throws ParseException, IOException {
        // refer to https://commons.apache.org/proper/commons-cli/usage.html to
        // build CLI
        // -r --articleRepositoryFolders path1,path2,...pathn
        // -a --contentAnalizers name1,name2,...namen
        // -i --indexPath path

        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(
                Option.builder("r")
                        .argName("pathes")
                        .hasArg()
                        .required()
                        .longOpt("articleRepositoryFolders")
                        .desc("pathes of article repository, separated by comma")
                        .build())
                .addOption(
                        Option.builder("a")
                                .argName("names")
                                .hasArg()
                                .required()
                                .longOpt("contentAnalyzers")
                                .desc("content analyzer names, separated by comma")
                                .build())
                .addOption(
                        Option.builder("i")
                                .argName("paths")
                                .hasArg()
                                .required()
                                .longOpt("indexPaths")
                                .desc("index paths, separated by comma. Each index path corresponds to one checker; each content analizer will have an index under a path;")
                                .build());

        CommandLine line = parser.parse(options, args);
        List<String> articleRepositoryFolders = Lists.newArrayList(Splitter.on(
                ',').split(line.getOptionValue("articleRepositoryFolders")));
        List<Path> folders = Lists
                .newArrayListWithCapacity(articleRepositoryFolders.size());
        articleRepositoryFolders.forEach(folder -> {
            File file = new File(folder);
            checkArgument(file.exists() && file.isDirectory(),
                    "repository folder");
            folders.add(Paths.get(folder));
        });

        List<String> contentAnalizerNames = Lists.newArrayList(Splitter.on(',')
                .split(line.getOptionValue("contentAnalyzers")));
        List<String> indexPaths = Lists.newArrayList(Splitter.on(',').split(
                line.getOptionValue("indexPaths")));
        List<List<FingerprintRepositoryInfo>> fingerprintRepositoryInfoLists = Lists
                .newArrayListWithCapacity(indexPaths.size());

        indexPaths
                .forEach(item -> {
                    Path indexPath = Paths.get(item);
                    checkArgument(indexPath.toFile().exists()
                            && indexPath.toFile().isDirectory(), "indexPath");

                    List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList = Lists
                            .newArrayListWithCapacity(contentAnalizerNames
                                    .size());

                    contentAnalizerNames.forEach(name -> {
                        File indexFile = indexPath.resolve(name).toFile();
                        checkArgument(
                                indexFile.exists() && !indexFile.isDirectory(),
                                "indexFile");
                        fingerprintRepositoryInfoList
                                .add(new FingerprintRepositoryInfo(
                                        ContentAnalyzerType.valueOf(name)
                                                .getContentAnalyzer(),
                                        indexFile));

                    });
                    fingerprintRepositoryInfoLists
                            .add(fingerprintRepositoryInfoList);
                });

        List<PlagiarismChecker> checkers = Lists
                .newArrayListWithCapacity(indexPaths.size());
        for (List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList : fingerprintRepositoryInfoLists) {
            checkers.add(new PlagiarismChecker(fingerprintRepositoryInfoList));
        }
        Context.ARTICLE_REPOSITORY = new ArticleRepositoryImpl(folders);
        Context.CHECKERS = checkers;
    }

    static class Context {
        // support multiple checkers, each checker has its own index root, to
        // support incremental index
        static List<PlagiarismChecker> CHECKERS;
        static ArticleRepository ARTICLE_REPOSITORY;
    }

    @javax.ws.rs.Path("check")
    static class PlagiarismCheckeService {
        @GET
        @javax.ws.rs.Path("/")
        @Produces(MediaType.APPLICATION_JSON)
        public Iterable<Result> check(
                @QueryParam("paragraph") String paragraphContent) {
            checkNotNull(paragraphContent, "paragraphContent");
            List<Iterable<Entry<ContentAnalyzer, Iterable<ParagraphEntry>>>> checkResultsList = Lists
                    .newArrayList();
            for (int i = 0; i < Context.CHECKERS.size(); ++i) {
                checkResultsList.add(null);
            }
            IntStream
                    .range(0, Context.CHECKERS.size())
                    .parallel()
                    .forEach(
                            i -> {
                                checkResultsList.set(i, Context.CHECKERS.get(i)
                                        .check(paragraphContent));
                            });
            List<Entry<ContentAnalyzer, Iterable<ParagraphEntry>>> checkResults = Lists
                    .newArrayList();
            checkResultsList.forEach(item -> {
                item.forEach(entry -> {
                    checkResults.add(entry);
                });
            });

            Map<ParagraphEntry, List<ContentAnalyzerType>> paragraphToContentAnalizersMap = Maps
                    .newHashMap();

            Map<Integer, List<ParagraphEntry>> articleToParagraphesMap = Maps
                    .newHashMap();
            checkResults
                    .forEach(pair -> {
                        pair.getValue()
                                .forEach(
                                        paragraphEntry -> {
                                            Integer articleId = paragraphEntry
                                                    .getArticleId();
                                            List<ParagraphEntry> paragraphEntryList = articleToParagraphesMap
                                                    .get(articleId);
                                            if (paragraphEntryList == null) {
                                                paragraphEntryList = Lists
                                                        .newArrayList();
                                                articleToParagraphesMap.put(
                                                        articleId,
                                                        paragraphEntryList);
                                            }
                                            paragraphEntryList
                                                    .add(paragraphEntry);
                                            List<ContentAnalyzerType> contentAnalizers = paragraphToContentAnalizersMap
                                                    .get(paragraphEntry);
                                            if (contentAnalizers == null) {
                                                contentAnalizers = Lists
                                                        .newArrayList();
                                                paragraphToContentAnalizersMap
                                                        .put(paragraphEntry,
                                                                contentAnalizers);
                                            }
                                            contentAnalizers.add(ContentAnalyzerType
                                                    .getContentAnalyzerType(pair
                                                            .getKey()));
                                        });
                    });

            List<Result> results = Lists.newArrayList();

            articleToParagraphesMap.entrySet().forEach(
                    item -> {
                        Article article = Context.ARTICLE_REPOSITORY
                                .getArticle(item.getKey());
                        assert article != null;
                        List<Paragraph> paragraphes = Lists
                                .newArrayList(article.getParagraphes());
                        assert paragraphes != null;
                        item.getValue().forEach(
                                paragraphEntry -> {
                                    Paragraph paragraph = paragraphes
                                            .get(paragraphEntry
                                                    .getParagraphId());
                                    assert paragraph != null;
                                    results.add(new Result(article.getId(),
                                            paragraph.getId(), paragraph
                                                    .getContent(),
                                            paragraphToContentAnalizersMap
                                                    .get(paragraphEntry)));
                                });
                    });
            return results;
        }
    }

    static class Result {
        private final int articleId;
        private final int paragraphId;
        private final String paragraphContent;
        private final Iterable<ContentAnalyzerType> hittedContentAnalizerTypes;

        Result(int articleId, int paragraphId, String paragraphContent,
                Iterable<ContentAnalyzerType> hittedContentAnalizerTypes) {
            this.articleId = articleId;
            this.paragraphId = paragraphId;
            this.paragraphContent = paragraphContent;
            this.hittedContentAnalizerTypes = hittedContentAnalizerTypes;
        }

        public int getArticleId() {
            return this.articleId;
        }

        public int getParagraphId() {
            return this.paragraphId;
        }

        public String getParagraphContent() {
            return this.paragraphContent;
        }

        public Iterable<ContentAnalyzerType> getHittedContentAnalizerTypes() {
            return this.hittedContentAnalizerTypes;
        }
    }

}
