package com.zeen.plagiarismchecker.application.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.Article;
import com.zeen.plagiarismchecker.ArticleRepository;
import com.zeen.plagiarismchecker.ContentAnalizer;
import com.zeen.plagiarismchecker.FingerprintRepositoryBuilder;
import com.zeen.plagiarismchecker.Paragraph;
import com.zeen.plagiarismchecker.impl.ArticleRepositoryImpl;
import com.zeen.plagiarismchecker.impl.ContentAnalizers;
import com.zeen.plagiarismchecker.impl.FingerprintRepositoryBuilderImpl;

public class IndexBuilder {

	static class FingerprintRepositoryInfo {
		final ContentAnalizer contentAnalizer;
		final File indexFile;

		FingerprintRepositoryInfo(final ContentAnalizer contentAnalizer,
				final File indexFile) {
			this.contentAnalizer = contentAnalizer;
			this.indexFile = indexFile;
		}
	}

	final ArticleRepository articleRepository;
	final List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList;
	final int capability;

	private IndexBuilder(ArticleRepository articleRepository,
			List<FingerprintRepositoryInfo> fingerprintRepositoryInfoList,
			int capability) {
		this.articleRepository = articleRepository;
		this.fingerprintRepositoryInfoList = fingerprintRepositoryInfoList;
		this.capability = capability;
	}

	public void build() throws IOException {
		List<FingerprintRepositoryBuilder> fingerprintRepositoryBuilderList = Lists
				.newArrayListWithCapacity(this.fingerprintRepositoryInfoList
						.size());
		this.fingerprintRepositoryInfoList.forEach(item -> {
			fingerprintRepositoryBuilderList
					.add(new FingerprintRepositoryBuilderImpl());
		});
		// read article repository once, and build all index at the same time;
		// it is faster but requires much more memory
		for (int i = 0; i < fingerprintRepositoryBuilderList.size(); ++i) {
			fingerprintRepositoryBuilderList.get(i).start(
					this.fingerprintRepositoryInfoList.get(i).contentAnalizer,
					this.capability);
		}

		for (Article article : this.articleRepository.getArticles()) {
			for (Paragraph paragraph : article.getParagraphes()) {
				fingerprintRepositoryBuilderList
						.forEach(fingerprintRepositoryBuilder -> {
							fingerprintRepositoryBuilder.add(paragraph);
						});
			}
		}
		for (int i = 0; i < fingerprintRepositoryBuilderList.size(); ++i) {
			fingerprintRepositoryBuilderList.get(i).build()
					.save(this.fingerprintRepositoryInfoList.get(i).indexFile);
		}
	}

	static IndexBuilder getIndexBuilderWithArgs(String[] args)
			throws ParseException, IOException {
		// refer to https://commons.apache.org/proper/commons-cli/usage.html to
		// build CLI
		// -r --articleRepositoryFolders path1,path2,...pathn
		// -a --contentAnalizers name1,name2,...namen
		// -i --indexPath path
		// -c --capability

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
								.build())
				.addOption(
						Option.builder("c")
								.argName("int")
								.hasArg()
								.required()
								.longOpt("capability")
								.desc("the total number of paragraphes that can be supported")
								.build());

		CommandLine line = parser.parse(options, args);
		List<String> articleRepositoryFolders = Lists.newArrayList(Splitter.on(
				',').split(line.getOptionValue("articleRepositoryFolders")));
		List<String> contentAnalizerNames = Lists.newArrayList(Splitter.on(',')
				.split(line.getOptionValue("contentAnalizers")));
		Path indexPath = Paths.get(line.getOptionValue("indexPath"));
		checkArgument(!indexPath.toFile().exists()
				|| indexPath.toFile().isDirectory(), "indexPath");

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
			// existing index will be overwritten
				fingerprintRepositoryInfoList
						.add(new FingerprintRepositoryInfo(ContentAnalizers
								.valueOf(name).getContentAnalizer(), indexPath
								.resolve(name).toFile()));
			});

		if (!indexPath.toFile().exists()) {
			indexPath.toFile().mkdirs();
		}

		int capability = Integer.valueOf(line.getOptionValue("capability"));

		ArticleRepository articleRepository = new ArticleRepositoryImpl(folders);
		return new IndexBuilder(articleRepository,
				fingerprintRepositoryInfoList, capability);
	}

	public static void main(final String[] args) throws ParseException,
			IOException {
		getIndexBuilderWithArgs(args).build();
	}
}
