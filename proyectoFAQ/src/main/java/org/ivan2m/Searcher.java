package org.ivan2m;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {
    private DirectoryReader reader;
    private IndexSearcher indexSearcher;

    public Searcher(String indexDirectoryPath) throws IOException {
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
            reader = DirectoryReader.open(indexDirectory);
            indexSearcher = new IndexSearcher(reader);
//            Similarity similarity = new ClassicSimilarity();
//            indexSearcher.setSimilarity(similarity);
//            System.out.println(indexSearcher.getSimilarity(true));

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void close() throws IOException {
        reader.close();
    }

    public Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

    public TopDocs search(Query searchQuery) throws IOException {
        return indexSearcher.search(searchQuery, LuceneConstants.MAX_SEARCH);
    }
}
