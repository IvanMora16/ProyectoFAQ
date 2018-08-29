package org.ivan2m;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilterFactory;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class MyAnalyzer {
    private CustomAnalyzer analyzer;

    public CustomAnalyzer getAnalyzer(){
        return this.analyzer;
    }

    public MyAnalyzer() {
        try {
            this.analyzer = CustomAnalyzer.builder()
                    .withTokenizer(StandardTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(StopFilterFactory.class, "ignoreCase", "true", "words", LuceneConstants.stopWordsFile, "format", "wordset")
                    //El filtro KeywordRepeatFilterFactory junto con el de stem (Snowball) hace que en el índice se guarde tanto la palabra
                    //original como la resultante de aplicar el stem
                    .addTokenFilter(KeywordRepeatFilterFactory.class)
                    .addTokenFilter(SnowballPorterFilterFactory.class, "language", "Spanish")
                    //El filtro RemoveDuplicates es para eliminar duplicados en caso de que el stemmer haya devuelto lo mismo que el original
                    .addTokenFilter(RemoveDuplicatesTokenFilterFactory.class)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String stemText(String text) throws IOException {
        String result = "";
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

        //Si por algun motivo no ha hecho bien el stem
        if (result.length()==0)
            result = (text);

        return result;
    }

    public void close(){
        analyzer.close();
    }
}
