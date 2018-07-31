package org.ivan2m;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.QueryBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LuceneTester {
    String indexDir = "C:\\Users\\ivan_\\Desktop\\Universidad\\TFG\\index";
    String dataDir = "C:\\Users\\ivan_\\Desktop\\Universidad\\TFG\\data";
    Indexer indexer;
    Searcher searcher;

    public static void main(String[] args) {
        System.out.println("Hola mundo");
        LuceneTester tester;

        try{
            tester = new LuceneTester();
            tester.createIndex();

            BufferedReader br;
            String searchString = "";
            System.out.print("Introduce la cadena a buscar: ");
            System.out.println();

            br = new BufferedReader(new InputStreamReader(System.in));
            searchString = br.readLine();
            br.close();

            //Separamos las palabras de la posible frase
            /*String[] words = searchString.split(" ");
            for(int i = 0; i < words.length; i++){
                System.out.println("Resultados de la búsqueda de la palabra: " + words[i]);
                tester.searchFuzzyQuery(words[i]);
            }*/
            tester.searchFuzzyQuery(searchString);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void createIndex() throws IOException {
        indexer = new Indexer(indexDir, false);
        Map<String, Integer> numIndexed =  indexer.createIndex(dataDir, new TextFileFilter());
        indexer.close();

        System.out.println("Hay un total de " + numIndexed.get("total") + " archivos indexados, "
                + numIndexed.get("new") + " son nuevos");
    }

    private void searchFuzzyQuery(String searchQuery) throws IOException {
        searcher = new Searcher(indexDir);
        QueryBuilder queryBuilder = new QueryBuilder(indexer.getAnalyzer());

        Query query = queryBuilder.createPhraseQuery(LuceneConstants.QUESTION, searchQuery, 500);
        
        TopDocs coincidences = searcher.search(query);

        System.out.println(coincidences.totalHits + " documentos encontrados");
        for(ScoreDoc scoreDoc : coincidences.scoreDocs){
            Document doc = searcher.getDocument(scoreDoc);
            String content = doc.get("content");
            System.out.println("Puntuación: " + scoreDoc.score + " | Archivo: " + doc.get(LuceneConstants.FILE_PATH));
        }

        searcher.close();
    }
}
