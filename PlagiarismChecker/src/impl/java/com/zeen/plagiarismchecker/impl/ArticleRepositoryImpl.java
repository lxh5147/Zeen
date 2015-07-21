package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.zeen.plagiarismchecker.Paragraph;
import com.zeen.plagiarismchecker.Article;
import com.zeen.plagiarismchecker.ArticleRepository;

public class ArticleRepositoryImpl implements ArticleRepository,
		Iterable<Article> {

	class ArticleIterator implements Iterator<Article> {
		private Iterator<Path> folderIterator;
		private Iterator<Path> pathes;

		ArticleIterator() {
			this.folderIterator = folders.iterator();
			if (this.folderIterator.hasNext()) {
				Path folder = this.folderIterator.next();
				this.pathes = this.getFiles(folder);
			} else {
				this.pathes = null;
			}
		}

		private Iterator<Path> getFiles(Path folder) {
			List<Path> files = Lists.newArrayList();
			try {
				this.getFiles(files, folder);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return files.iterator();
		}

		private List<Path> getFiles(List<Path> files, Path dir)
				throws IOException {
			DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
			for (Path path : stream) {
				// suppose all files are put under the dir, no sub folders are
				// allowed
				assert path.toFile().isFile();
				files.add(path);
			}
			return files;
		}

		@Override
		public boolean hasNext() {
			if (this.pathes == null) {
				return false;
			}
			if (this.pathes.hasNext()) {
				return true;
			}
			if (!this.folderIterator.hasNext()) {
				return false;
			}
			Path folder = this.folderIterator.next();
			this.pathes = this.getFiles(folder);
			return this.hasNext();
		}

		@Override
		public Article next() {
			assert (this.pathes != null && this.pathes.hasNext());
			try {
				return readArticle(this.pathes.next().toFile());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	// suppose one line one paragraph
	// file name is id, file is gzipped
	private Article readArticle(File file) throws IOException {
		int articleId = Integer.valueOf(file.getName());
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(file)), "UTF-8"))) {
			String paragraphContent = null;
			int paragraphId = 0;
			List<Paragraph> paragraphes = Lists.newArrayList();
			while ((paragraphContent = reader.readLine()) != null) {
				paragraphes.add(new ParagraphImpl(articleId, paragraphId,
						paragraphContent));
			}
			return new ArticleImpl().withId(articleId)
					.withParagraphes(paragraphes).build();
		}
	}

	@Override
	public Iterator<Article> iterator() {
		return new ArticleIterator();
	}

	@Override
	public Iterable<Article> getArticles() {
		return this;
	}

	@Override
	public Article getArticle(int id) {
		for (Path path : this.folders) {
			File file = path.resolve(String.valueOf(id)).toFile();
			if (file.exists() && !file.isDirectory()) {
				try {
					return this.readArticle(file);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}

	public ArticleRepositoryImpl(Iterable<Path> folders) {
		this.folders = checkNotNull(folders, "folders");
	}

	// suppose id is the file name, distributed under several folders
	private final Iterable<Path> folders;

	static class ParagraphImpl implements Paragraph {

		private final int articleId;
		private final int id;
		private final String content;

		ParagraphImpl(int articleId, int id, String content) {
			this.id = id;
			this.content = content;
			this.articleId = articleId;
		}

		@Override
		public String getContent() {
			return this.content;
		}

		@Override
		public int getId() {
			return this.id;
		}

		@Override
		public int getArticleId() {
			return this.articleId;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this.getClass())
					.add("articleId", this.articleId).add("id", this.id)
					.add("content", this.content).toString();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.articleId, this.id, this.content);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ParagraphImpl other = (ParagraphImpl) obj;
			return Objects.equal(this.getArticleId(), other.getArticleId())
					&& Objects.equal(this.getId(), other.getId())
					&& Objects.equal(this.getContent(), other.getContent());
		}
	}

	static class ArticleImpl implements Article, Article.Builder {
		private int id;
		private String doi;
		private String title;
		private String author;
		private String abstraction;
		private Iterable<Paragraph> paragraphes;

		@Override
		public int getId() {
			return this.id;
		}

		@Override
		public String getDOI() {
			return this.doi;
		}

		@Override
		public String getTitle() {
			return this.title;
		}

		@Override
		public String getAuthor() {
			return this.author;
		}

		@Override
		public String getAbstraction() {
			return this.abstraction;
		}

		@Override
		public Iterable<Paragraph> getParagraphes() {
			return this.paragraphes;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this.getClass())
					.add("id", this.id).add("paragraphes", this.paragraphes)
					.toString();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.id, this.paragraphes);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final ArticleImpl other = (ArticleImpl) obj;
			return Objects.equal(this.getId(), other.getId())
					&& Objects.equal(this.getParagraphes(),
							other.getParagraphes());
		}

		@Override
		public Article build() {
			return this;
		}

		@Override
		public Builder withId(int id) {
			checkArgument(id >= 0, "id");
			this.id = id;
			return this;
		}

		@Override
		public Builder withDOI(String doi) {
			this.doi = doi;
			return this;
		}

		@Override
		public Builder withTitle(String title) {
			this.title = title;
			return this;
		}

		@Override
		public Builder withAbstract(String abstraction) {
			this.abstraction = abstraction;
			return this;
		}

		@Override
		public Builder withParagraphes(Iterable<Paragraph> paragraphes) {
			this.paragraphes = checkNotNull(paragraphes, "paragraphes");
			return this;
		}
	}

}
