package org.ivan2m;

import java.io.File;

public class LuceneConstants {
    public static final String ID = "id";
    public static final String QUESTION = "question";
    public static final String ANSWER = "answer";
    public static final String FILE_NAME = "filename";
    public static final String FILE_PATH = "filepath";
    public static final int MAX_SEARCH = 10;
    public static final String indexDir = "C:\\Users\\ivan_\\Desktop\\Universidad\\TFG\\index";
    public static final String dataDir = "src\\main\\resources\\faqs";
    public static final String stopWordsFile = "stopWords.txt";
//    public static final String stopWordsFile = "src\\main\\resources\\stopWords.txt";
//    private static ClassLoader classLoader = getClass().getClassLoader();
//    public static String stopWordsFile = new File(classLoader.getResource("somefile").getFile()).getAbsolutePath();
//    public String stopWordsFile = getClass().getResource("stopWords.txt").toString();
}
