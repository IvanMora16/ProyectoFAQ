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

    /**
     * Para obtener el analyzer
     * @return
     */
    public CustomAnalyzer getAnalyzer(){
        return this.analyzer;
    }

    /**
     * Creación de un Analyzer de Lucene con filtros personalizados
     */
    public MyAnalyzer() {
        try {
            this.analyzer = CustomAnalyzer.builder()
                    //Separa el texto en tokens, palabras. Separa por espacios y tiene en cuenta los signos(",", ".", ":"...), los elimina
                    .withTokenizer(StandardTokenizerFactory.class)
                    //Convierte a minusculas el texto
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

    /**
     * Para aplicar los filtros del analyzer a un texto
     * @param text texto al que se le aplican los filtros
     * @return el resultado de aplicar los filtros a text
     * @throws IOException
     */
    public String applyFilters(String text) throws IOException {
        String result = "";
        TokenStream tStream = this.analyzer.tokenStream("content", text);
        CharTermAttribute term = tStream.addAttribute(CharTermAttribute.class);

        tStream.reset();

        try {
            while (tStream.incrementToken()){
                result += term.toString() + " ";
            }
        } catch (IOException ioe){
            System.out.println("Error: "+ioe.getMessage());
        }

        //Si por algun motivo no ha aplicado bien los filtros devolvemos el texto original
        if (result.length()==0)
            result = (text);

        return result;
    }

    public void close(){
        analyzer.close();
    }
}
