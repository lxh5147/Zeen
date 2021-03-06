package com.zeen.plagiarismchecker.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import com.zeen.plagiarismchecker.Article;
import com.zeen.plagiarismchecker.ArticleRepository;
import com.zeen.plagiarismchecker.Paragraph;

public class ArticleRepositoryImpl implements ArticleRepository,
        Iterable<Article> {

    public static Iterator<Path> getFiles(Path folder) {
        checkNotNull(folder, "folder");
        List<Path> files = Lists.newArrayList();
        try {
            getFiles(files, folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return files.iterator();
    }

    private static void getFiles(List<Path> files, Path dir) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path path : stream) {
            if (path.toFile().isFile()) {
                files.add(path);
            } else {
                getFiles(files, path);
            }
        }

    }

    class ArticleIterator implements Iterator<Article> {
        private Iterator<Path> folderIterator;
        private Iterator<Path> paths;

        ArticleIterator() {
            this.folderIterator = folders.iterator();
            if (this.folderIterator.hasNext()) {
                Path folder = this.folderIterator.next();
                this.paths = getFiles(folder);
            } else {
                this.paths = null;
            }
        }

        @Override
        public boolean hasNext() {
            if (this.paths == null) {
                return false;
            }
            if (this.paths.hasNext()) {
                return true;
            }
            if (!this.folderIterator.hasNext()) {
                return false;
            }
            Path folder = this.folderIterator.next();
            this.paths = getFiles(folder);
            return this.hasNext();
        }

        @Override
        public Article next() {
            assert (this.paths != null && this.paths.hasNext());
            try {
                return readArticle(this.paths.next().toFile());
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
            List<Paragraph> paragraphs = Lists.newArrayList();
            while ((paragraphContent = reader.readLine()) != null) {
                paragraphs.add(new ParagraphImpl(articleId, paragraphId,
                        paragraphContent));
                ++paragraphId;
            }
            return new ArticleImpl().withId(articleId)
                    .withParagraphs(paragraphs).build();
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
    final Iterable<Path> folders;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("folders", this.folders).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.folders);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArticleRepositoryImpl other = (ArticleRepositoryImpl) obj;

        return Objects.equal(this.folders, other.folders);
    }

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
        private Iterable<Paragraph> paragraphs;

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
        public Iterable<Paragraph> getParagraphs() {
            return this.paragraphs;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("id", this.id).add("paragraphs", this.paragraphs)
                    .toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.id, this.paragraphs);
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
                    && Objects.equal(this.getParagraphs(),
                            other.getParagraphs());
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
        public Builder withParagraphs(Iterable<Paragraph> paragraphs) {
            this.paragraphs = checkNotNull(paragraphs, "paragraphs");
            return this;
        }
    }

}
