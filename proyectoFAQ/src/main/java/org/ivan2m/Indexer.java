package org.ivan2m;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
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
    private Analyzer analyzer;
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

//            CharArraySet stopWords = new CharArraySet(getStopWords(), true);
//            analyzer = new SpanishAnalyzer(stopWords);
            analyzer = CustomAnalyzer.builder()
                    .withTokenizer(StandardTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(StopFilterFactory.class, "ignoreCase", "true", "words", LuceneConstants.stopWordsFile, "format", "wordset")
                    .addTokenFilter(SnowballPorterFilterFactory.class, "language", "Spanish")
                    .build();

//            System.out.println(analyzer.getStopwordSet().iterator());
            /*Iterator it = analyzer.getStopwordSet().iterator();
            while(it.hasNext()){
                System.out.println(it.next());
            }*/

            IndexWriterConfig indexWriterConf = new IndexWriterConfig(analyzer);
            indexWriterConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

//            Similarity similarity = new ClassicSimilarity();
            Similarity similarity = new BM25Similarity(1.2f, 0.75f);
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
     * Para obtener la lista de stop words de un archivo
     * @return
     */
    private List<String> getStopWords(){
        List<String> stopWords = new ArrayList<>();
        String fileContent = "";

        Path path =  Paths.get(LuceneConstants.stopWordsFile);

        try {
            fileContent = new String(Files.readAllBytes(path));
            String[] stop_words = fileContent.split("\r\n");

            for(int i = 0; i < stop_words.length; i++){
                stopWords.add(stop_words[i]);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        return stopWords;
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
     * Para obtener los datos necesarios de cada par pregunta/respuesta de un archivo FAQ
     * @param file
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
                Field questionField = new TextField(LuceneConstants.QUESTION, treatText(question), Field.Store.YES);
                Field answerField = new TextField(LuceneConstants.ANSWER, treatText(answer), Field.Store.YES);

                //indexamos el nombre del archivo
//                String themes = FilenameUtils.getBaseName(file.getName().toLowerCase()).replaceAll("\\.", " ");
//                Field fileNameField = new TextField(LuceneConstants.FILE_NAME, themes, Field.Store.YES);
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
     * @param filter
     * @throws IOException
     */
    public void createIndex(FileFilter filter) throws IOException {
        int newFiles = 0;
        int totalFAQs = 0;
        boolean indexed;
        File faqs =  new File(LuceneConstants.dataDir);

        if(faqs.isDirectory()) {
            File[] files = new File(LuceneConstants.dataDir).listFiles();

            for (File file : files) {
                indexed = false;
                if (!file.isDirectory() && file.exists() && file.canRead() && filter.accept(file)) {
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

    /**
     * Para tratar el texto antes de indexarlo
     * @param original
     * @return
     */
    private String treatText(String original){
        String newText = original.replaceAll(",", "");
        newText = newText.replaceAll("\\.", "");
        newText = newText.replaceAll("¿", "");
        newText = newText.replaceAll("\\?", "");
        newText = newText.replaceAll(";", "");
        newText = newText.replaceAll(":", "");
        newText = newText.replaceAll("\\(", "");
        newText = newText.replaceAll("\\)", "");

        return newText.toLowerCase();
    }
}
