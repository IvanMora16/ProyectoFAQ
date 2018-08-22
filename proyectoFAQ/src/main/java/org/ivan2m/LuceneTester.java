package org.ivan2m;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.util.QueryBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LuceneTester {
    String indexDir = "C:\\Users\\ivan_\\Desktop\\Universidad\\TFG\\index";
    String dataDir = "C:\\Users\\ivan_\\Desktop\\Universidad\\TFG\\data";
    Indexer indexer;
    Searcher searcher;

    public ArrayList<String> searchQuestion(String searchString) {
        System.out.println("Hola mundo");
        ArrayList<String> result = new ArrayList<>();
//        String result = "";

        try{
            this.createIndex();
            result = this.searchFuzzyQuery(searchString);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return result;
    }

    private void createIndex() throws IOException {
        indexer = new Indexer(indexDir, false);
        indexer.createIndex(dataDir, new TextFileFilter());
        Map<String, Integer> numIndexed = indexer.getIndexInfo();
                indexer.close();

        System.out.println("Hay un total de " + numIndexed.get("totalQuestions") + " preguntas indexadas de " +
                numIndexed.get("totalFAQs") + " FAQs (archivos), " + numIndexed.get("newFAQs") + " FAQs nuevos");
    }

    private ArrayList<String> searchFuzzyQuery(String searchQuery) throws IOException {
        ArrayList<String> result = new ArrayList<>();
//        String result = "";
        searcher = new Searcher(indexDir);
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

        coincidences = searcher.search(query);

        System.out.println(coincidences.totalHits + " preguntas encontradas");
        for(ScoreDoc scoreDoc : coincidences.scoreDocs){
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("Puntuación: " + scoreDoc.score + " | FAQ: " + doc.get(LuceneConstants.FILE_PATH) +
                    " | Pregunta id: " + doc.get(LuceneConstants.ID) + " | Pregunta: " + doc.get(LuceneConstants.QUESTION));

            result.add("Puntuación: " + scoreDoc.score + " | FAQ: " + doc.get(LuceneConstants.FILE_PATH) +
                    " | Pregunta id: " + doc.get(LuceneConstants.ID) + " | Pregunta: " + doc.get(LuceneConstants.QUESTION) +
                    " | Respuesta: " + doc.get(LuceneConstants.ANSWER));
        }

        if(coincidences.scoreDocs.length == 0){
            result.add("No se ha encontrado ninguna pregunta similar");
        }

        searcher.close();

        return result;
    }

    private void searchFuzzyQuery2(String searchQuery) throws IOException {
        searcher = new Searcher(indexDir);
        TopDocs coincidences;

        //Separamos las palabras de la posible frase para anyadirlas a la query por separado, cada una en un fuzzyquery
        String[] words = searchQuery.split(" ");
        SpanQuery[] multiWordFuzzyQuery = new SpanQuery[words.length];
        MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder();

        for (int i = 0; i < words.length; i++) {
            builder.add(new Term(LuceneConstants.QUESTION, words[i]));
        }

        MultiPhraseQuery query = builder.build();

        coincidences = searcher.search(query);

        System.out.println(coincidences.totalHits + " preguntas encontradas");
        for(ScoreDoc scoreDoc : coincidences.scoreDocs){
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("Puntuación: " + scoreDoc.score + " | FAQ: " + doc.get(LuceneConstants.FILE_PATH) +
                    " | Pregunta id: " + doc.get(LuceneConstants.ID) + " | Pregunta: " + doc.get(LuceneConstants.QUESTION));
        }

        searcher.close();
    }
}
