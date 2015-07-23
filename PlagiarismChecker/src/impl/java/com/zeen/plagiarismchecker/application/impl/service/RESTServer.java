package com.zeen.plagiarismchecker.application.impl.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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
import com.zeen.plagiarismchecker.ParagraphEntry;
import com.zeen.plagiarismchecker.Paragraph;
import com.zeen.plagiarismchecker.application.impl.FingerprintRepositoryInfo;
import com.zeen.plagiarismchecker.application.impl.PlagiarismChecker;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryImpl;
import com.zeen.plagiarismchecker.impl.ContentAnalizers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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

    private static void setupContext(String[] args) throws ParseException,
            IOException {
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
        List<String> articleRepositoryFolders = Lists.newArrayList(Splitter.on(
                ',').split(line.getOptionValue("articleRepositoryFolders")));
        List<String> contentAnalizerNames = Lists.newArrayList(Splitter.on(',')
                .split(line.getOptionValue("contentAnalizers")));
        Path indexPath = Paths.get(line.getOptionValue("indexPath"));
        checkArgument(indexPath.toFile().exists()
                && indexPath.toFile().isDirectory(), "indexPath");

        List<Path> folders = Lists
                .newArrayListWithCapacity(articleRepositoryFolders.size());
        articleRepositoryFolders.forEach(folder -> {
            File file = new File(folder);
            checkArgument(file.exists() && file.isDirectory(),
                    "repository folder");
            folders.add(Paths.get(folder));
        });
        List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList = Lists
                .newArrayListWithCapacity(contentAnalizerNames.size());

        contentAnalizerNames.forEach(name -> {
            File indexFile = indexPath.resolve(name).toFile();
            checkArgument(indexFile.exists() && !indexFile.isDirectory(),
                    "indexFile");
            fingerprintRepositoryInfoList.add(new FingerprintRepositoryInfo(
                    ContentAnalizers.valueOf(name).getContentAnalizer(),
                    indexFile));
        });

        Context.ARTICLE_REPOSITORY = new ArticleRepositoryImpl(folders);
        Context.CHECKER = new PlagiarismChecker(fingerprintRepositoryInfoList);
    }

    private static class Context {
        static PlagiarismChecker CHECKER;
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
            Iterable<ParagraphEntry> ParagraphEntries = Context.CHECKER
                    .check(paragraphContent);
            // group by article and then read article
            Map<Integer, List<ParagraphEntry>> index = Maps.newHashMap();
            ParagraphEntries.forEach(paragraphEntry -> {
                Integer articleId = paragraphEntry.getArticleId();
                List<ParagraphEntry> paragraphEntryList = index.get(articleId);
                if (paragraphEntryList == null) {
                    paragraphEntryList = Lists.newArrayList();
                    index.put(articleId, paragraphEntryList);
                }
                paragraphEntryList.add(paragraphEntry);
            });
            List<Result> results = Lists.newArrayList();

            index.entrySet().forEach(
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
                                                    .getContent()));
                                });
                    });
            return results;
        }
    }

    static class Result {
        private final int articleId;
        private final int paragraphId;
        private final String paragraphContent;

        Result(int articleId, int paragraphId, String paragraphContent) {
            this.articleId = articleId;
            this.paragraphId = paragraphId;
            this.paragraphContent = paragraphContent;
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
    }

}
