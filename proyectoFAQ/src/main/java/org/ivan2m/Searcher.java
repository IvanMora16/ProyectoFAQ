package org.ivan2m;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {
    private DirectoryReader reader;
    private IndexSearcher indexSearcher;

    /**
     * Contructor del Searcher que servirá para hacer consultas al índice
     * @param indexDirectoryPath Localización del índice
     * @throws IOException
     */
    public Searcher(String indexDirectoryPath) throws IOException {
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
            reader = DirectoryReader.open(indexDirectory);
            indexSearcher = new IndexSearcher(reader);

//            Similarity similarity = new ClassicSimilarity();
//            Similarity similarity = new ClassicSimilarity(){
//                @Override
//                public float lengthNorm(int numTerms){
//                    return (float)1/numTerms;
//                }
//
//                @Override
//                public float tf(float freq){
//                    return freq;
//                }
//            };
            Similarity similarity = new BM25Similarity(1.2f, 0.75f);
            indexSearcher.setSimilarity(similarity);
//            System.out.println(indexSearcher.getSimilarity(true));

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Para cerrar el reader
     * @throws IOException
     */
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Para obtener la información de un documento del índice
     * @param scoreDoc
     * @return Document el documento
     * @throws IOException
     */
    public Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

    /**
     * Para realizar una búsqueda en el índice
     * @param searchQuery
     * @return TopDocs el resultado de la búsqueda con los documentos
     * @throws IOException
     */
    public TopDocs search(Query searchQuery) throws IOException {
        return indexSearcher.search(searchQuery, LuceneConstants.MAX_SEARCH);
    }
}
