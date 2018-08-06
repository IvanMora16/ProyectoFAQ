package org.ivan2m;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Indexer {
    private IndexWriter writer;
    private String indexDir = "";
    private Analyzer analyzer;
    private Map<String, Integer> indexInfo;

    /**
     * Para crear o abrir el indexador para poder añadir documentos
     */
    public Indexer(String indexDirectoryPath, boolean deleteIndex) throws IOException{
        try {
            indexInfo = new HashMap<>();
            //El directorio donde estarán las indexaciones
            this.indexDir = indexDirectoryPath;
            Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
            analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConf = new IndexWriterConfig(analyzer);

            //Creamos el indexador o lo abrimos en caso de ya existir
            writer = new IndexWriter(indexDirectory, indexWriterConf);
            if(deleteIndex) {
                writer.deleteAll();
            }
            writer.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Map getIndexInfo(){
        return indexInfo;
    }

    /**
     * Para comprobar si existe algún índice
     * @param indexDirectoryPath
     * @return verdadero o falso según exista el índice
     */
    public boolean indexExist(String indexDirectoryPath){
        boolean existe = false;

        try{
            Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
            existe = DirectoryReader.indexExists(indexDirectory);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return existe;
    }

    /**
     * Para cerrar el indexador y guardar los cambios
     * @throws IOException
     */
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

    /**
     * Para obtener los datos necesarios del documento
     * @param file
     * @return Document, el documento con los datos de: pregunta, respuesta, nombre del archivo, ruta del archivo
     * @throws IOException
     */
    private void getDocumentQuestions(File file) throws IOException {
        JSONParser parser = new JSONParser();
        String question = "";
        String answer = "";
        int id;
        Document doc;

        try{
            Object object = parser.parse(new FileReader(file));
            JSONArray jsonArray = (JSONArray) object;

            for(int i = 0; i < jsonArray.size(); i++){
                doc = new Document();
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                id = i;
                question = (String) jsonObject.get("question");
                answer = (String) jsonObject.get("answer");

                //indexamos el contenido del archivo: pregunta y respuesta, y le asignamos un id
                Field idField = new StoredField(LuceneConstants.ID, id);
                Field questionField = new TextField(LuceneConstants.QUESTION, question, Field.Store.YES);
                Field answerField = new TextField(LuceneConstants.ANSWER, answer, Field.Store.YES);
                //indexamos el nombre del archivo
                Field fileNameField = new StringField(LuceneConstants.FILE_NAME, file.getName().toLowerCase(), Field.Store.YES);
                //indexamos la ruta del archivo
                Field filePathField = new StringField(LuceneConstants.FILE_PATH, file.getCanonicalPath().toLowerCase(), Field.Store.YES);

                doc.add(idField);
                doc.add(questionField);
                doc.add(answerField);
                doc.add(fileNameField);
                doc.add(filePathField);

                writer.addDocument(doc);
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Para indexar un archivo en caso de no estarlo ya
     * @param file
     * @return boolean que indica si el archivo se ha indexado o no
     * @throws IOException
     */
    private boolean indexFile(File file) throws IOException {
        boolean indexed = true;

        //Buscamos si el archivo a indexar ya lo está
        Searcher searcher = new Searcher(indexDir);
        TopDocs results = searcher.search(new TermQuery(new Term(LuceneConstants.FILE_PATH, file.getCanonicalPath().toLowerCase())));

        //Si no está indexado, lo indexamos
        if (results.totalHits == 0) {
            System.out.println("Indexing " + file.getCanonicalPath());
            getDocumentQuestions(file);
        }
        else{
            indexed = false;
        }

        return indexed;
    }

    /**
     * Para indexar los archivos de un directorio en el índice, si existe
     * @param dataDir
     * @param filter
     * @return int, el total de archivos indexados en el índice
     * @throws IOException
     */
    public void createIndex(String dataDir, FileFilter filter) throws IOException {
        int newFiles = 0;
        int totalFAQs = 0;
        boolean indexed;
        File[] files = new File(dataDir).listFiles();

        for(File file : files){
            indexed = false;
            if(!file.isDirectory() && file.exists() && file.canRead() && filter.accept(file)){
                totalFAQs++;
                indexed = indexFile(file);
                if(indexed)
                    newFiles++;
            }
        }

        indexInfo.put("totalQuestions", writer.numDocs());
        indexInfo.put("newFAQs", newFiles);
        indexInfo.put("totalFAQs", totalFAQs);
    }
}
