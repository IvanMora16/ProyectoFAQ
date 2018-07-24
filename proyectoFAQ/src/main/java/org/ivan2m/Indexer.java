package org.ivan2m;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Paths;

public class Indexer {
    private IndexWriter writer;

    public Indexer(String indexDirectoryPath) throws IOException{
        try {
            //El directorio donde estarán las indexaciones
            Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConf = new IndexWriterConfig(analyzer);
            //Creamos el indexador
            writer = new IndexWriter(indexDirectory, indexWriterConf);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void close() throws IOException {
        //Comiteamos los cambios en el indexador al hacer close()
        writer.close();
    }

//    private Document getDocument(File file) throws IOException {
//        Document doc = new Document();
//        //indexamos el contenido del archivo
//        Field contentField = new TextField(LuceneConstants.CONTENTS, new FileReader(file));
//        //indexamos el nombre del archivo
//        Field fileNameField = new TextField(LuceneConstants.FILE_NAME, file.getName(), Field.Store.YES);
//        //indexamos la ruta del archivo
//        Field filePathField = new TextField(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Field.Store.YES);
//
//        doc.add(contentField);
//        doc.add(fileNameField);
//        doc.add(filePathField);
//
//        return doc;
//    }

    private Document getDocument(File file) throws IOException {
        JSONParser parser = new JSONParser();
        String question = "";
        String answer = "";
        Document doc = new Document();

        try{
            Object object = parser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) object;
            Long id = (Long) jsonObject.get("id");
            question = (String) jsonObject.get("question");
            answer = (String) jsonObject.get("answer");

            System.out.println("Pregunta: " + question);
            System.out.println("Respuesta: " + answer);

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        //indexamos el contenido del archivo: pregunta y respuesta
        Field questionField = new TextField(LuceneConstants.QUESTION, question, Field.Store.YES);
        Field answerField = new TextField(LuceneConstants.ANSWER, answer, Field.Store.YES);
        //indexamos el nombre del archivo
        Field fileNameField = new TextField(LuceneConstants.FILE_NAME, file.getName(), Field.Store.YES);
        //indexamos la ruta del archivo
        Field filePathField = new TextField(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Field.Store.YES);

        doc.add(questionField);
        doc.add(answerField);
        doc.add(fileNameField);
        doc.add(filePathField);

        return doc;
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing " + file.getCanonicalPath());
        Document doc = getDocument(file);
        writer.addDocument(doc);
    }

    public int createIndex(String dataDir, FileFilter filter) throws IOException {
        File[] files = new File(dataDir).listFiles();
        for(File file : files){
            if(!file.isDirectory() && file.exists() && file.canRead() && filter.accept(file)){
                indexFile(file);
            }
        }

        return writer.numDocs();
    }
}
