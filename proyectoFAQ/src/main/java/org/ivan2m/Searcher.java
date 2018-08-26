package org.ivan2m;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Searcher {
    private DirectoryReader reader;
    private IndexSearcher indexSearcher;

    /**
     * Contructor del Searcher que servirá para hacer consultas al índice
     * @param indexDirectoryPath Localización del índice
     * @throws IOException
     */
    public Searcher(String indexDirectoryPath){
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

    /**
     * Realizamos una consulta al Searcher
     * @param searchQuery
     * @return
     * @throws IOException
     */
    public ArrayList<String> searchFuzzyQuery(String searchQuery) throws IOException {
        ArrayList<String> result = new ArrayList<>();
//        String result = "";
        TopDocs coincidences;

        //Separamos las palabras de la posible frase para anyadirlas a la query por separado, cada una en un fuzzyquery
        String[] words = searchQuery.split(" ");
        SpanQuery[] multiWordFuzzyQuery = new SpanQuery[words.length];

        for (int i = 0; i < words.length; i++) {
            multiWordFuzzyQuery[i] = new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term(LuceneConstants.QUESTION, words[i])));
        }

        //Con longitud 0 y 1 el SpanNearQuery peta, necesita al menos 2 palabras para ver si estan cerca entre ellas
        //En cambio el SpanOrQuery va bien
        //Con SpanNearQuery, con que falte una de las palabras ya te devuelve 0 hits, no es muy bueno
//        SpanNearQuery query = new SpanNearQuery(multiWordFuzzyQuery, 500, false);
        SpanOrQuery query = new SpanOrQuery(multiWordFuzzyQuery);

        coincidences = this.search(query);

        System.out.println(coincidences.totalHits + " preguntas encontradas");
        for(ScoreDoc scoreDoc : coincidences.scoreDocs){
            Document doc = this.getDocument(scoreDoc);
            System.out.println("Puntuación: " + scoreDoc.score + " | FAQ: " + doc.get(LuceneConstants.FILE_PATH) +
                    " | Pregunta id: " + doc.get(LuceneConstants.ID) + " | Pregunta: " + doc.get(LuceneConstants.QUESTION));

//            result.add("Puntuación: " + scoreDoc.score + " | FAQ: " + doc.get(LuceneConstants.FILE_PATH) +
//                    " | Pregunta id: " + doc.get(LuceneConstants.ID) + " | Pregunta: " + doc.get(LuceneConstants.QUESTION) +
//                    " | Respuesta: " + doc.get(LuceneConstants.ANSWER));

            result.add("Puntuación: " + scoreDoc.score + " | Pregunta: " + doc.get(LuceneConstants.QUESTION) +
                    " | Respuesta: " + doc.get(LuceneConstants.ANSWER));
        }

        if(coincidences.scoreDocs.length == 0){
            result.add("No se ha encontrado ninguna pregunta similar");
        }

        //Cuando deberia cerrarlo?
//        searcher.close();

        return result;
    }
}
