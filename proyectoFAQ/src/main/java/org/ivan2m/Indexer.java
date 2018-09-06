package org.ivan2m;

import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Indexer {
    private IndexWriter writer;
//    private SpanishAnalyzer analyzer;
    private CustomAnalyzer analyzer;
    private Map<String, Integer> indexInfo;

    /**
     * Para crear o abrir el indexador para poder añadir documentos
     * @param deleteIndex True, borramos y creamos el índice de nuevo, porque algún FAQ ha cambiado por ejemplo. False,
     *                    tan solo abrimos el índice.
     * @throws IOException
     */
    public Indexer(boolean deleteIndex) throws IOException{
        try {
            indexInfo = new HashMap<>();
            //El directorio donde estarán las indexaciones
            Directory indexDirectory = FSDirectory.open(Paths.get(LuceneConstants.indexDir));

            MyAnalyzer myAnalyzer = new MyAnalyzer();
            analyzer = myAnalyzer.getAnalyzer();

            IndexWriterConfig indexWriterConf = new IndexWriterConfig(analyzer);
            indexWriterConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            Similarity similarity = new BM25Similarity(1.2f, 0.75f);
            indexWriterConf.setSimilarity(similarity);

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

    /**
     * Para obtener la información del índice
     * @return Map que contiene información del índice
     */
    public Map getIndexInfo(){
        return indexInfo;
    }

    /**
     * Para cerrar el indexador y guardar los cambios
     * @throws IOException
     */
    public void close() throws IOException {
        //Comiteamos los cambios en el indexador al hacer close()
        writer.close();
        analyzer.close();
    }

    /**
     * Para obtener los datos necesarios de cada par pregunta/respuesta de un archivo FAQ
     * @param file
     * @throws IOException
     */
    private void getDocumentQuestions(File file) throws IOException {
        JSONParser parser = new JSONParser();
        String question = "";
        String answer = "";
        Document doc;

        try{
            Object object = parser.parse(new FileReader(file));
            JSONArray jsonArray = (JSONArray) object;

            for(int i = 0; i < jsonArray.size(); i++){
                doc = new Document();
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                question = (String) jsonObject.get("question");
                answer = (String) jsonObject.get("answer");

                //indexamos el contenido del archivo: pregunta y respuesta
                Field questionField = new TextField(LuceneConstants.QUESTION, question, Field.Store.YES);
                Field answerField = new TextField(LuceneConstants.ANSWER, answer, Field.Store.YES);

                //indexamos el nombre del archivo
//                String themes = FilenameUtils.getBaseName(file.getName().toLowerCase()).replaceAll("\\.", " ");
//                Field fileNameField = new TextField(LuceneConstants.FILE_NAME, themes, Field.Store.YES);
                Field fileNameField = new StringField(LuceneConstants.FILE_NAME, file.getName().toLowerCase(), Field.Store.YES);

                //indexamos la ruta del archivo
                Field filePathField = new StringField(LuceneConstants.FILE_PATH, file.getCanonicalPath().toLowerCase(), Field.Store.YES);

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
        Searcher searcher = new Searcher();
        TopDocs results = searcher.search(new TermQuery(new Term(LuceneConstants.FILE_PATH, file.getCanonicalPath().toLowerCase())));

        //Si no está indexado, lo indexamos
        if (results.totalHits == 0) {
            System.out.println("Indexing " + file.getCanonicalPath());
            getDocumentQuestions(file);
        }
        else{
            indexed = false;
        }
        searcher.close();

        return indexed;
    }

    /**
     * Para indexar los archivos (faqs) de un directorio en el índice
     * @throws IOException
     */
    public void createIndex() throws IOException {
        int newFiles = 0;
        int totalFAQs = 0;
        boolean indexed;
        File faqs =  new File(LuceneConstants.dataDir);

        if(faqs.isDirectory()) {
            File[] files = new File(LuceneConstants.dataDir).listFiles();

            for (File file : files) {
                indexed = false;
                if (!file.isDirectory() && file.exists() && file.canRead() &&
                        file.getName().toLowerCase().endsWith(".json")) {
                    totalFAQs++;
                    indexed = indexFile(file);
                    if (indexed)
                        newFiles++;
                }
            }
        }

        indexInfo.put("totalQuestions", writer.numDocs());
        indexInfo.put("newFAQs", newFiles);
        indexInfo.put("totalFAQs", totalFAQs);
    }
}
