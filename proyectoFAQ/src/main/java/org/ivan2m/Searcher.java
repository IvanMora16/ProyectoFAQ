package org.ivan2m;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Searcher {
    private DirectoryReader reader;
    private IndexSearcher indexSearcher;
    private IndexReader indexReader;

    /**
     * Contructor del Searcher que servirá para hacer consultas al índice
     * @throws IOException
     */
    public Searcher(){
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(LuceneConstants.indexDir));
            reader = DirectoryReader.open(indexDirectory);
            indexReader = reader;
            indexSearcher = new IndexSearcher(reader);

            Similarity similarity = new BM25Similarity(1.2f, 0.75f);
            indexSearcher.setSimilarity(similarity);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Para obtener el IndexReader y leer el contenido del índice
     * @return
     */
    public IndexReader getIndexReader(){
        return indexReader;
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
     * @param userQuestion es la pregunta realizada por el usuario y que se va a consultar en el índice
     * @return
     * @throws IOException
     */
    public ArrayList<String> searchQuery(String userQuestion) throws IOException {
        String searchQuery = userQuestion;
        ArrayList<String> result = new ArrayList<>();
        TopDocs coincidences;

        //Aplicamos los filtros del analyzer a la cadena de la búsqueda
        MyAnalyzer analyzer = new MyAnalyzer();
        searchQuery = analyzer.applyFilters(searchQuery);
        analyzer.close();

        //Separamos las palabras de la posible frase para anyadirlas a la query por separado, cada una en un spanquery
        String[] words = searchQuery.split(" ");
        SpanQuery[] multiWordQuery = new SpanQuery[words.length];

        for (int i = 0; i < words.length; i++) {
            //Para query para buscar con fuzzy query
//            multiWordQuery[i] = new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term(LuceneConstants.QUESTION, words[i])));
            //Para query para buscar por palabra exacta
            multiWordQuery[i] = new SpanTermQuery(new Term(LuceneConstants.QUESTION, words[i]));
        }

        //Con longitud 0 y 1 el SpanNearQuery peta, necesita al menos 2 palabras para ver si estan cerca entre ellas
        //En cambio el SpanOrQuery va bien
        //Con SpanNearQuery, con que falte una de las palabras ya te devuelve 0 hits, no es muy bueno
        SpanOrQuery query = new SpanOrQuery(multiWordQuery);

        coincidences = this.search(query);

        System.out.println(coincidences.totalHits + " preguntas encontradas");
        for(ScoreDoc scoreDoc : coincidences.scoreDocs){
            Document doc = this.getDocument(scoreDoc);

            System.out.println("Puntuación: " + scoreDoc.score + " | Pregunta id: " + doc.get(LuceneConstants.ID) +
                    " | Pregunta: " + doc.get(LuceneConstants.QUESTION));

            result.add("Pregunta: " + doc.get(LuceneConstants.QUESTION) + "\r\nRespuesta: " + doc.get(LuceneConstants.ANSWER));
        }

        if(coincidences.scoreDocs.length == 0){
            result.add("No se ha encontrado ninguna pregunta similar");
        }

        return result;
    }
}
