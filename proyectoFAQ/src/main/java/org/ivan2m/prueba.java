package org.ivan2m;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class prueba {

    public static void main(String[] args){
        MyAnalyzer analyzer = new MyAnalyzer();
        try {
            System.out.println(analyzer.applyFilters("¿Qué es un crédito?"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void StemCustom(String text) throws IOException {
        String result = "";
        MyAnalyzer myAnalyzer = new MyAnalyzer();
        CustomAnalyzer analyzer = myAnalyzer.getAnalyzer();
        TokenStream tStream = analyzer.tokenStream("content", text);
        CharTermAttribute term = tStream.addAttribute(CharTermAttribute.class);

        tStream.reset();

        try {
            while (tStream.incrementToken()){
                result += term.toString() + " ";
            }
        } catch (IOException ioe){
            System.out.println("Error: "+ioe.getMessage());
        }

        analyzer.close();
        System.out.println(result);
//        TokenStream ts = new RemoveDuplicatesTokenFilter(new SnowballFilter(new KeywordRepeatFilter(
//                original), "Spanish"));


//        StringReader reader = new StringReader("the birds are flying");
//        TokenStream ts = new RemoveDuplicatesTokenFilter(new SnowballFilter(new KeywordRepeatFilter(
//                )), "Spanish");
    }

    public static String Stem(String text) throws IOException {
        StringBuffer result = new StringBuffer();
        if (text != null && text.trim().length() > 0){
            StringReader tReader = new StringReader(text);
            Analyzer analyzer = new SpanishAnalyzer();
            TokenStream tStream = analyzer.tokenStream("contents", tReader);
            CharTermAttribute term = tStream.addAttribute(CharTermAttribute.class);

            tStream.reset();

            try {
                while (tStream.incrementToken()){
                    result.append(term.toString());
                    result.append(" ");
                }
            } catch (IOException ioe){
                System.out.println("Error: "+ioe.getMessage());
            }

        }

        // If, for some reason, the stemming did not happen, return the original text
        if (result.length()==0)
            result.append(text);
        return result.toString().trim();
    }

    public static void prueba1(){
        File file = new File("C:\\Users\\ivan_\\Desktop\\Universidad\\TFG\\data\\Preinscripcion.Biblioteca.json");
        System.out.println(FilenameUtils.getBaseName(file.getName().toLowerCase()));
        String name = FilenameUtils.getBaseName(file.getName().toLowerCase());
        String[] keywords = name.split("\\.");
        int largo = keywords.length;
        for(int i = 0; i < largo; i++){
            System.out.println(keywords[i]);
        }
    }

    public static void prueba2(){
        Searcher searcher = new Searcher();
        IndexReader reader = searcher.getIndexReader();

        try {
            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                String docId = doc.get("docId");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
