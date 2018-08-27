package org.ivan2m;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.File;
import java.io.IOException;

public class prueba {

    public static void main(String[] args){
        String cadena = "hola, que tal estas";
        System.out.println(cadena);
        cadena = cadena.replaceAll(",", "");
        System.out.println(cadena);
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
